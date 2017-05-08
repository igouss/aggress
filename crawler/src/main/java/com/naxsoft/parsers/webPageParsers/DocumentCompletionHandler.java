package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import okhttp3.Response;
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
        LOGGER.info("Completed request to {}", response.request().url().toString());
        Document document = Jsoup.parse(response.body().string(), response.request().url().toString());
        return new DownloadResult(source, document);
    }
}
