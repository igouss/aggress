package com.naxsoft.http;

import com.naxsoft.entity.WebPageEntity;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    public DownloadResult onCompleted(Response response) {
        LOGGER.info("Completed request to {}", response.request().url().toString());
        Document document = null;
        if (response.body() != null) {

            try {
                document = Jsoup.parse(response.body().byteStream(), "UTF-8", response.request().url().toString());
            } catch (IOException e) {
                LOGGER.error("Failed to parse {}", source);
            }
        }
        return new DownloadResult(source, document);
    }
}
