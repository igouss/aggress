package com.naxsoft.parsers.webPageParsers.canadaAmmo;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
public class CanadaAmmoProductPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(CanadaAmmoProductPageParser.class);

    public CanadaAmmoProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(PageDownloader.download(client, webPage.getUrl())).map(webPageEntity -> {
            webPageEntity.setCategory(webPage.getCategory());
            return webPageEntity;
        });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("productPage");
    }
}
