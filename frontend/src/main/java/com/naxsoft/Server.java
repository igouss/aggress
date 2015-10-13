package com.naxsoft;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.io.File;

/**
 * Copyright NAXSoft 2015
 */
public class Server {
    public static void main(final String[] args) {
        Logger logger = LoggerFactory.getLogger(Server.class);

        TemplateEngine templateEngine = new TemplateEngine();
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setTemplateMode("HTML5");

        templateEngine.addTemplateResolver(templateResolver);



        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
        TransportClient client = new TransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

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

        ApplicationContext context = new ApplicationContext(true);

        PathHandler pathHandler = Handlers.path();
        pathHandler.addExactPath("/", new IndexHandler(context, templateEngine));
        pathHandler.addExactPath("/search", new SearchHandler(client));

        String baseDir = Paths.get("").toAbsolutePath().toString();
        String relPath = "\\frontend\\basedir\\thymeleaf";
        pathHandler.addPrefixPath("/css", Handlers.resource(new FileResourceManager(new File(baseDir + relPath + "\\css\\"), 100)));
        pathHandler.addPrefixPath("/fonts", Handlers.resource(new FileResourceManager(new File(baseDir + relPath + "\\fonts\\"), 100)));
        pathHandler.addPrefixPath("/js", Handlers.resource(new FileResourceManager(new File(baseDir + relPath + "\\js\\"), 100)));
        pathHandler.addPrefixPath("/img", Handlers.resource(new FileResourceManager(new File(baseDir + relPath + "\\img\\"), 100)));

        SessionManager sessionManager = new InMemorySessionManager("SESSION_MANAGER");
        SessionCookieConfig sessionConfig = new SessionCookieConfig();
        SessionAttachmentHandler sessionAttachmentHandler = new SessionAttachmentHandler(sessionManager, sessionConfig);
        sessionAttachmentHandler.setNext(pathHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(pathHandler) // new SimpleErrorPageHandler().setNext

//                .setHandler(sessionAttachmentHandler)
//                .setHandler(Handlers.path()
//                        .addPrefixPath("/api/ws", websocket(new WebSocketHandler()))
//                        .addPrefixPath("/api/rest", new RestHandler()))
                .build();

        server.start();
    }

}
//.setHandler(resource(new PathResourceManager(Paths.get(System.getProperty("user.home")), 100))
//        .setDirectoryListingEnabled(true))