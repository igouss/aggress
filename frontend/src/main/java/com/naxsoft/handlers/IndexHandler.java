package com.naxsoft.handlers;

import com.naxsoft.ApplicationContext;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Locale;


public class IndexHandler {
    private static final String REGULAR_NAME = "layout";

    private final ApplicationContext appContext;
    private final TemplateEngine templateEngine;

    /**
     * @param context
     * @param templateEngine
     */
    public IndexHandler(ApplicationContext context, TemplateEngine templateEngine) {
        this.appContext = context;
        this.templateEngine = templateEngine;
    }

    public void handleRequestVertX(RoutingContext context) {
        if (appContext.isInvalidateTemplateCache()) {
            templateEngine.clearTemplateCacheFor(REGULAR_NAME);
        }

        HttpServerResponse response = context.response();
        response.setChunked(true);
        response.putHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.putHeader("Pragma", "no-cache");
        response.putHeader("Expires", "0");
        response.putHeader("content-type", "text/html;charset=UTF-8");

        HashMap<String, Object> variables = new HashMap<>();
        Context htmlContext = new Context(Locale.getDefault(), variables);
        String result = templateEngine.process(REGULAR_NAME, htmlContext);
        response.end(result);
    }
}

