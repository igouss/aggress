package com.naxsoft.parsers.webPageParsers;

import com.google.gson.Gson;
import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Copyright NAXSoft 2015
 */
public class JsonCompletionHandler extends AbstractCompletionHandler<JsonResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler.class);
    private final WebPageEntity source;

    /**
     * @param source Requested page
     */
    public JsonCompletionHandler(WebPageEntity source) {
        this.source = source;
    }

    @Override
    public JsonResult onCompleted(Response response) throws Exception {
        LOGGER.info("Completed request to {}", response.request().url().toString());
        Map json = new Gson().fromJson(response.body().string(), Map.class);
        return new JsonResult(source, json);
    }
}