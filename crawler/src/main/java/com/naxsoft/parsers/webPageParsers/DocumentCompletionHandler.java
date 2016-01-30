package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Copyright NAXSoft 2015
 */
public class DocumentCompletionHandler extends AbstractCompletionHandler<Document> {
    @Override
    public Document onCompleted(Response response) throws Exception {
        return Jsoup.parse(response.getResponseBody(), response.getUri().toUrl());
    }
}
