package com.naxsoft.handlers;

import com.naxsoft.ApplicationContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;

/**
 * Copyright NAXSoft 2015
 */
public class IndexHandler implements HttpHandler {
    public static final String TEMPLATE_NAME = Paths.get("").toAbsolutePath() + "\\frontend\\basedir\\thymeleaf\\layout.html";
    private ApplicationContext context;
    private TemplateEngine templateEngine;

    public IndexHandler(ApplicationContext context, TemplateEngine templateEngine) {
        this.context = context;
        this.templateEngine = templateEngine;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (context.isInvalidateTemplateCache()) {
            templateEngine.clearTemplateCacheFor(TEMPLATE_NAME);
        }
        HashMap<String, String> variables = new HashMap<>();
        Context context = new Context(Locale.getDefault(), variables);

        String result = templateEngine.process(TEMPLATE_NAME, context);
        exchange.getResponseSender().send(result);
    }
}