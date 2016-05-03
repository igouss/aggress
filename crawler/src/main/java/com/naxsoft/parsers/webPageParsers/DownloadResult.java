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
     * @param sourcePage
     * @param document
     */
    public DownloadResult(WebPageEntity sourcePage, Document document) {
        this.sourcePage = sourcePage;
        this.document = document;
    }

    /**
     *
     * @return
     */
    public WebPageEntity getSourcePage() {
        return sourcePage;
    }

    /**
     *
     * @return
     */
    public Document getDocument() {
        return document;
    }
}