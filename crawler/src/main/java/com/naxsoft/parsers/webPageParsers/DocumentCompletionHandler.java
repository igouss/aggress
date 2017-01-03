package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import org.asynchttpclient.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
public class DocumentCompletionHandler extends AbstractCompletionHandler<DownloadResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentCompletionHandler.class);
    private final WebPageEntity source;

    /**
     * @param source Requested page
     */
    public DocumentCompletionHandler(WebPageEntity source) {
        this.source = source;
    }

    @Override
    public DownloadResult onCompleted(Response response) throws Exception {
        LOGGER.trace("Completed request to {}", response.getUri().toString());
        Document document = Jsoup.parse(response.getResponseBody(), response.getUri().toUrl());
        return new DownloadResult(source, document);
    }
}
