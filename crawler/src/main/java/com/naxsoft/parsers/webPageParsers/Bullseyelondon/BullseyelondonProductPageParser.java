package com.naxsoft.parsers.webPageParsers.bullseyelondon;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class BullseyelondonProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BullseyelondonProductPageParser.class);
    private final HttpClient client;

    public BullseyelondonProductPageParser(HttpClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(PageDownloader.download(client, webPage))
                .filter(data -> null != data);
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("productPage");
    }
}
