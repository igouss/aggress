package com.naxsoft;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.naxsoft.handlers.IndexHandler;
import com.naxsoft.handlers.SearchHandler;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
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
     */
    public static void main(final String[] args) throws PropertyNotFoundException {
        TemplateEngine templateEngine = getTemplateEngine();
        ElasticsearchClient esClient = getElasticsearchClient();

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
     * Get Elasticsearch Java API Client
     *
     * @return Elasticsearch Java API Client
     */
    private static ElasticsearchClient getElasticsearchClient() throws PropertyNotFoundException {
        String elasticHost = AppProperties.getProperty("elasticHost");
        int elasticPort = Integer.valueOf(AppProperties.getProperty("elasticPort"));

        // Create the low-level REST client
        RestClient restClient = RestClient.builder(
                new HttpHost(elasticHost, elasticPort, "http")
        ).build();

        // Create the transport with Jackson mapper
        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        // Create the high-level API client
        ElasticsearchClient client = new ElasticsearchClient(transport);

        // Test connection and wait for cluster to be available
        while (true) {
            LOGGER.info("Waiting for elastic to connect to {}:{}...", elasticHost, elasticPort);
            try {
                var response = client.info();
                if (response != null) {
                    LOGGER.info("Connection established to {}:{}, cluster: {}",
                            elasticHost, elasticPort, response.clusterName());
                    break;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to connect to Elasticsearch: {}", e.getMessage());
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
     * @return TemplateEngine
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