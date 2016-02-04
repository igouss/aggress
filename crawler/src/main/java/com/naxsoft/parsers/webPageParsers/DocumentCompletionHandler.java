package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Copyright NAXSoft 2015
 */
public class DocumentCompletionHandler extends AbstractCompletionHandler<DownloadResult> {
    private WebPageEntity source;

    public DocumentCompletionHandler(WebPageEntity source) {
        this.source = source;
    }

    @Override
    public DownloadResult onCompleted(Response response) throws Exception {
        Document document = Jsoup.parse(response.getResponseBody(), response.getUri().toUrl());
        DownloadResult result = new DownloadResult(source, document);
        return result;
    }
}
