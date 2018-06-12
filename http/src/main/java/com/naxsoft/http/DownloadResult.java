package com.naxsoft.http;

import com.naxsoft.entity.WebPageEntity;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;

public class DownloadResult {
    private final WebPageEntity sourcePage;

    @Nullable
    private final Document document;

    /**
     * @param sourcePage Page that was requested
     * @param document   Parsed HTML Page
     */
    public DownloadResult(WebPageEntity sourcePage, @Nullable Document document) {
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
    @Nullable
    public Document getDocument() {
        return document;
    }
}