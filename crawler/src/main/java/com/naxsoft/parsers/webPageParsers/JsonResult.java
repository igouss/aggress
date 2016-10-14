package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;

import java.util.Map;

/**
 * Copyright NAXSoft 2015
 */
public class JsonResult {
    private final WebPageEntity sourcePage;
    private final Map json;

    /**
     * @param sourcePage Page that was requested
     * @param json       Parsed HTML Page
     */
    JsonResult(WebPageEntity sourcePage, Map json) {
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
    public Map getJson() {
        return json;
    }
}