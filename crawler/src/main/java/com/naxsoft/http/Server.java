package com.naxsoft.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Copyright NAXSoft 2015
 */
public class Server extends AbstractVerticle {
    private io.vertx.core.http.HttpServer httpServer = null;

    private final static IContext context = new Context(Locale.CANADA);

    @Override
    public void start() throws Exception {
        httpServer = vertx.createHttpServer();
        TemplateEngine templateEngine = getTemplateEngine();

        httpServer.requestHandler(req -> {
            HttpServerResponse response = req.response();
            response.putHeader("content-type", "text/html");
            response.end(templateEngine.process("index", context));
        }).listen(8080);



    }

    @Override
    public void stop() throws Exception {
        if (null != httpServer) {
            httpServer.close();
        }
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
        templateResolver.setPrefix(Paths.get("").toAbsolutePath() + File.separator + "basedir" + File.separator + "html" + File.separator);
        templateResolver.setSuffix(".html");
        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }

}