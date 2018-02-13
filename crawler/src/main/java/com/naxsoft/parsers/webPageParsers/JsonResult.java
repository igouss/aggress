package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;

import java.util.List;
import java.util.Map;

public class JsonResult {
    private final WebPageEntity sourcePage;
    private final Map<String, List<Map<String, String>>> json;

    /**
     * @param sourcePage Page that was requested
     * @param json       Parsed HTML Page
     */
    JsonResult(WebPageEntity sourcePage, Map<String, List<Map<String, String>>> json) {
        this.sourcePage = sourcePage;
        this.json = json;
    }

    /**
     * Requested page
     *
     * @return Requested page
     */
    public WebPageEntity getSourcePage() {
        return sourcePage;
    }

    /**
     * Parsed result
     *
     * @return Parsed result
     */
    public Map<String, List<Map<String, String>>> getJson() {
        return json;
    }
}