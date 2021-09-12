package com.naxsoft.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naxsoft.common.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonCompletionHandler extends AbstractCompletionHandler<JsonResult> {
    private static final TypeToken<Map<String, List<Map<String, String>>>> TYPE_TOKEN = new TypeToken<Map<String, List<Map<String, String>>>>() {
    };
    private final WebPageEntity source;

    /**
     * @param source Requested page
     */
    public JsonCompletionHandler(WebPageEntity source) {
        this.source = source;
    }

    @Override
    public JsonResult onCompleted(Response response) {
        log.info("Completed request to {}", response.request().url().toString());

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            Map<String, List<Map<String, String>>> r = new Gson().fromJson(new BufferedReader(new InputStreamReader(responseBody.byteStream())), TYPE_TOKEN.getType());
            return new JsonResult(source, r);
        } else {
            throw new RuntimeException("Failed to parse empty response");
        }
    }
}