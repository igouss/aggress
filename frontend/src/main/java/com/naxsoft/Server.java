package com.naxsoft;

import com.naxsoft.handlers.IndexHandler;
import com.naxsoft.handlers.SearchHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015.
 * HTTP frontend to search engine
 */
public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    /**
     * Start server app
     *
     * @param args Application command line args
     * @throws UnknownHostException Thrown when we try to connect to invalid elasticsearch client
     */
    public static void main(final String[] args) throws UnknownHostException {
        TemplateEngine templateEngine = getTemplateEngine();
        TransportClient esClient = getTransportClient();

        ApplicationContext context = new ApplicationContext();
        context.setInvalidateTemplateCache(true);

        HttpHandler pathHandler = getPathHandler(templateEngine, esClient, context);

        SessionManager sessionManager = new InMemorySessionManager("SESSION_MANAGER");
        SessionCookieConfig sessionConfig = new SessionCookieConfig();
        SessionAttachmentHandler sessionAttachmentHandler = new SessionAttachmentHandler(sessionManager, sessionConfig);
        sessionAttachmentHandler.setNext(pathHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .addHttpListener(8090, "localhost")
                .setHandler(pathHandler) // new SimpleErrorPageHandler().setNext
                .build();
        try {
            server.start();
        } catch (RuntimeException e) {
            LOGGER.error("Server error", e);
        }
    }

    /**
     * Configure web app path routing
     *
     * @param templateEngine HTML template engine
     * @param client         Elasticsearch client
     * @param context        Application context
     * @return HTTP routing handler
     */
    private static HttpHandler getPathHandler(TemplateEngine templateEngine, TransportClient client, ApplicationContext context) {
        PathHandler pathHandler = Handlers.path();
        ContentEncodingRepository contentEncodingRepository = new ContentEncodingRepository();
        contentEncodingRepository.addEncodingHandler("gzip", new GzipEncodingProvider(), 50, Predicates.truePredicate());
        final EncodingHandler handler = new EncodingHandler(contentEncodingRepository);
        handler.setNext(pathHandler);

        pathHandler.addExactPath("/", new IndexHandler(context, templateEngine));
        pathHandler.addExactPath("/verbose", Handlers.disableCache(new IndexHandler(context, templateEngine)));
        pathHandler.addExactPath("/search", Handlers.disableCache(new SearchHandler(client)));

        String baseDir = Paths.get("").toAbsolutePath().toString();
        String relPath = baseDir + "/basedir/thymeleaf";
        pathHandler.addPrefixPath("/css", Handlers.resource(new FileResourceManager(new File(relPath + "/css/"), 100)));
        pathHandler.addPrefixPath("/fonts", Handlers.resource(new FileResourceManager(new File(relPath + "/fonts/"), 100)));
        pathHandler.addPrefixPath("/js", Handlers.disableCache(Handlers.resource(new FileResourceManager(new File(relPath + "/js/"), 100))));
        pathHandler.addPrefixPath("/img", Handlers.resource(new FileResourceManager(new File(relPath + "/img/"), 100)));

//                .setHandler(sessionAttachmentHandler)
//                .setHandler(Handlers.path()
//                        .addPrefixPath("/api/ws", websocket(new WebSocketHandler()))
//                        .addPrefixPath("/api/rest", new RestHandler()))
        return handler;
    }

    /**
     * Get Elasticsearch client
     *
     * @return Elasticsearch client
     * @throws UnknownHostException Thrown when we try to connect to invalid host
     */
    private static TransportClient getTransportClient() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
        TransportClient client = new TransportClient.Builder().settings(settings).build();
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        while (true) {
            LOGGER.info("Waiting for elastic to connect to a node...");
            int connectedNodes = client.connectedNodes().size();
            if (0 != connectedNodes) {
                LOGGER.info("Connection established");
                break;
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
            } catch (InterruptedException e) {
                LOGGER.error("Thread sleep failed", e);
            }
        }
        return client;
    }

    /**
     * Get HTML5 template engine
     *
     * @return TempleteEngine
     */
    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCharacterEncoding("UTF-8");
        // templateResolver.setCacheTTLMs(3600000L);
        templateResolver.setPrefix(Paths.get("").toAbsolutePath() + File.separator + "basedir" + File.separator + "thymeleaf" + File.separator);
        templateResolver.setSuffix(".html");
        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }
}
