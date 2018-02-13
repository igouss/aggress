package com.naxsoft.parsers.webPageParsers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

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

        Map<String, List<Map<String, String>>> r = new Gson().fromJson(response.body().string(), new TypeToken<Map<String, List<Map<String, String>>>>() {
        }.getType());
        return new JsonResult(source, r);
    }
}