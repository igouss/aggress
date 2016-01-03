package com.naxsoft;

import com.naxsoft.handlers.IndexHandler;
import com.naxsoft.handlers.SearchHandler;
import com.naxsoft.handlers.SearchVerboseHandler;
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
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
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
 * Copyright NAXSoft 2015
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    static Node node;

    public static void main(final String[] args) throws UnknownHostException {
//        startElasticSearch();


        TemplateEngine templateEngine = getTemplateEngine();
        TransportClient esClient = getTransportClient();

        ApplicationContext context = new ApplicationContext();
        context.setInvalidateTemplateCache(true);

//        VertxOptions vertxOptions = new VertxOptions();
//        vertxOptions.setMetricsOptions(new MetricsOptions().setEnabled(true));
//
//        Vertx vertx = Vertx.vertx(vertxOptions);
//        vertx.close(handler -> System.out.println("Vert.x is shutdown"));

        HttpHandler pathHandler = getPathHandler(templateEngine, esClient, context);

        SessionManager sessionManager = new InMemorySessionManager("SESSION_MANAGER");
        SessionCookieConfig sessionConfig = new SessionCookieConfig();
        SessionAttachmentHandler sessionAttachmentHandler = new SessionAttachmentHandler(sessionManager, sessionConfig);
        sessionAttachmentHandler.setNext(pathHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(pathHandler) // new SimpleErrorPageHandler().setNext
                .build();
        try {
            server.start();
        } catch (RuntimeException e) {
            logger.error("Server error", e);
        } finally {
            // server.stop();
        }
    }

//    private static void startElasticSearch() {
//        Path currentRelativePath = Paths.get("");
//        String s = currentRelativePath.toAbsolutePath().toString();
//
//        Settings.Builder settings =
//                Settings.settingsBuilder();
//        settings.put("path.home", s);
//        settings.put("path.data", "index");
//        settings.put("http.enabled", false);
//
//
//        node = NodeBuilder.nodeBuilder()
//                .settings(settings)
//                .clusterName("elasticsearch")
//                .data(true).local(false).node();
//
//    }

    private static HttpHandler getPathHandler(TemplateEngine templateEngine, TransportClient client, ApplicationContext context) {
        PathHandler pathHandler = Handlers.path();
        final EncodingHandler handler =
                new EncodingHandler(new ContentEncodingRepository()
                        .addEncodingHandler("gzip",
                                new GzipEncodingProvider(), 50,
                                Predicates.parse("max-content-size[5]")))
                        .setNext(pathHandler);

        pathHandler.addExactPath("/", new IndexHandler(context, templateEngine, false));
        pathHandler.addExactPath("/verbose", new IndexHandler(context, templateEngine, true));
        pathHandler.addExactPath("/search", new SearchHandler(client));
        pathHandler.addExactPath("/searchVerbose", new SearchVerboseHandler(client));

        String baseDir = Paths.get("").toAbsolutePath().toString();
        String relPath = baseDir + "/basedir/thymeleaf";
        pathHandler.addPrefixPath("/css", Handlers.resource(new FileResourceManager(new File(relPath + "/css/"), 100)));
        pathHandler.addPrefixPath("/fonts", Handlers.resource(new FileResourceManager(new File(relPath + "/fonts/"), 100)));
        pathHandler.addPrefixPath("/js", Handlers.resource(new FileResourceManager(new File(relPath + "/js/"), 100)));
        pathHandler.addPrefixPath("/img", Handlers.resource(new FileResourceManager(new File(relPath + "/img/"), 100)));

//                .setHandler(sessionAttachmentHandler)
//                .setHandler(Handlers.path()
//                        .addPrefixPath("/api/ws", websocket(new WebSocketHandler()))
//                        .addPrefixPath("/api/rest", new RestHandler()))


        return handler;
    }

    private static TransportClient getTransportClient() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
        TransportClient client = new TransportClient.Builder().settings(settings).build();
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        while (true) {
            logger.info("Waiting for elastic to connect to a node...");
            int connectedNodes = client.connectedNodes().size();
            if (0 != connectedNodes) {
                logger.info("Connection established");
                break;
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
            } catch (InterruptedException e) {
                logger.error("Thread sleep failed", e);
            }
        }
        return client;
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setTemplateMode("HTML5");

        templateEngine.addTemplateResolver(templateResolver);
        return templateEngine;
    }

    class HttpVertex extends AbstractVerticle {
        @Override
        public void start(Future<Void> startFuture) throws Exception {
            startFuture.complete();
        }

        @Override
        public void stop(Future<Void> stopFuture) throws Exception {
            super.stop(stopFuture);
        }
    }
}
