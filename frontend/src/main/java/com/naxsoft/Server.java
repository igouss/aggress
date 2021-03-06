package com.naxsoft;

import com.naxsoft.handlers.IndexHandler;
import com.naxsoft.handlers.SearchHandler;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.List;
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
    public static void main(final String[] args) throws UnknownHostException, PropertyNotFoundException {
        TemplateEngine templateEngine = getTemplateEngine();
        TransportClient esClient = getTransportClient();


        ApplicationContext context = new ApplicationContext();
        context.setInvalidateTemplateCache(true);

        IndexHandler indexHandler = new IndexHandler(context, templateEngine);
        SearchHandler searchHandler = new SearchHandler(esClient);

        Vertx vertx = Vertx.vertx();
//        SessionStore store = ClusteredSessionStore.create(vertx, "frontend.sessionMap");
//        SessionHandler sessionHandler = SessionHandler.create(store);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route("/css/*").handler(StaticHandler.create("basedir/thymeleaf/css"));
        router.route("/fonts/*").handler(StaticHandler.create("basedir/thymeleaf/fonts"));
        router.route("/img/*").handler(StaticHandler.create("basedir/thymeleaf/img"));
        router.route("/js/*").handler(StaticHandler.create("basedir/thymeleaf/js"));

        // Make sure all requests are routed through the session handler too
//        router.route().handler(sessionHandler);
        router.route("/").handler(indexHandler::handleRequestVertX);
        router.route("/search").handler(searchHandler::handleRequestVertX);

        server.requestHandler(router::accept).listen(8080);
    }


    /**
     * Get Elasticsearch client
     *
     * @return Elasticsearch client
     * @throws UnknownHostException Thrown when we try to connect to invalid host
     */
    private static TransportClient getTransportClient() throws UnknownHostException, PropertyNotFoundException {
        String elasticHost = AppProperties.getProperty("elasticHost");
        int elasticPort = Integer.valueOf(AppProperties.getProperty("elasticPort"));

        Settings settings = Settings.builder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), elasticPort));


        while (true) {
            LOGGER.info("Waiting for elastic to connect to a node {}:{}...", elasticHost, elasticPort);
            List<DiscoveryNode> discoveryNodes = client.connectedNodes();
            if (0 != discoveryNodes.size()) {
                LOGGER.info("Connection established {}", discoveryNodes.stream().map(DiscoveryNode::toString).reduce("", (a, b) -> {
                    if (a.isEmpty()) {
                        return b;
                    } else {
                        return a + ", " + b;
                    }
                }));
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
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        // templateResolver.setCacheTTLMs(3600000L);
        templateResolver.setPrefix(Paths.get("").toAbsolutePath() + File.separator + "basedir" + File.separator + "thymeleaf" + File.separator);
        templateResolver.setSuffix(".html");
        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }
}
