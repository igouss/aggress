package com.naxsoft.parsers.webPageParsers.irunguns;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
public class IrungunsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(IrungunsFrontPageParser.class);

    public IrungunsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> client.get("https://www.irunguns.us/product_categories.php", new CompletionHandler<Void>() {
            @Override
            public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                if (200 == resp.getStatusCode()) {

                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("#content .widthLimit a");
                    for (Element e : elements) {
                        String linkUrl = e.attr("abs:href");
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(linkUrl);
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setType("productPage");
                        LOGGER.info("ProductPageUrl={}, parseUrl={}", linkUrl, webPage.getUrl());
                        subscriber.onNext(webPageEntity);
                    }
                }
                subscriber.onCompleted();
                return null;
            }
        }));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("https://www.irunguns.us/") && webPage.getType().equals("frontPage");
    }
}
