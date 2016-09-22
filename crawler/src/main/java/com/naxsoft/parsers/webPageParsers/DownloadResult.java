package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import org.jsoup.nodes.Document;

/**
 * Copyright NAXSoft 2015
 */
public class DownloadResult {
    private WebPageEntity sourcePage;
    private Document document;

    /**
     * @param sourcePage Page that was requested
     * @param document   Parsed HTML Page
     */
    public DownloadResult(WebPageEntity sourcePage, Document document) {
        this.sourcePage = sourcePage;
        this.document = document;
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
    public Document getDocument() {
        return document;
    }
}