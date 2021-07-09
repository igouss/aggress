package com.naxsoft.http;

import com.naxsoft.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Slf4j
public class DocumentCompletionHandler extends AbstractCompletionHandler<DownloadResult> {
    private final WebPageEntity source;

    /**
     * @param source Requested page
     */
    public DocumentCompletionHandler(WebPageEntity source) {
        this.source = source;
    }

    @Override
    public DownloadResult onCompleted(Response response) {
        log.info("Completed request to {}", response.request().url().toString());
        Document document = null;
        if (response.body() != null) {

            try {
                document = Jsoup.parse(response.body().byteStream(), "UTF-8", response.request().url().toString());
            } catch (IOException e) {
                log.error("Failed to parse {}", source);
            }
        }
        return new DownloadResult(source, document);
    }
}
