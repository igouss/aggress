package com.naxsoft.parsers.webPageParsers.tradeexcanada;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
public class TradeexCanadaFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger logger = LoggerFactory.getLogger(TradeexCanadaFrontPageParser.class);

    public TradeexCanadaFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://www.tradeexcanada.com/products_list"));
        return Observable.create(subscriber -> Observable.from(webPageEntities).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(Response resp) throws Exception {
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                            Elements elements = document.select(".view-content a");
                            for (Element element : elements) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(element.attr("abs:href"));
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setType("productList");
                                logger.info("Product page listing={}", webPageEntity.getUrl());
                                subscriber.onNext(webPageEntity);
                            }
                        }
                        subscriber.onCompleted();
                        return null;
                    }
                }))));
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("frontPage");
    }
}