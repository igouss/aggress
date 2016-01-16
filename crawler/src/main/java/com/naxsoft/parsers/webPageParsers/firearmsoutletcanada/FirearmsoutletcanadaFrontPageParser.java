package com.naxsoft.parsers.webPageParsers.firearmsoutletcanada;

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
public class FirearmsoutletcanadaFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(FirearmsoutletcanadaFrontPageParser.class);
    private static final String[] categories = {
            "Precision and Target Rifles",
            "Hunting and Sporting Arms",
            "Military Surplus Rifle",
            "Pistols and Revolvers",
            "Shotguns",
            "Modern Military and Black Rifles",
            "Rimfire Firearms",
            "Optics and Sights",
            "Factory Ammo and Reloading Equipment",
    };
    private final HttpClient client;

    public FirearmsoutletcanadaFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/pistols.html?limit=all&stock_status=64"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/rifles.html?limit=all&stock_status=64"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/shotguns.html?limit=all&stock_status=64"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/ammo.html?limit=all&stock_status=64"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/accessories.html?limit=all&stock_status=64"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/reloading.html?limit=all&stock_status=64"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/parts.html?limit=all&stock_status=64"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/sights-optics.html?limit=all&stock_status=64"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/consignment.html?limit=all&stock_status=64"));
        return Observable.create(subscriber -> {
            Observable.from(webPageEntities).
                    flatMap(page -> Observable.from(client.get(page.getUrl(), new CompletionHandler<Void>() {
                        @Override
                        public Void onCompleted(Response resp) throws Exception {
                            if (200 == resp.getStatusCode()) {
                                Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                                Elements elements = document.select(".products-grid .product-name > a");

                                for (Element el : elements) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(el.attr("abs:href"));
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("productPage");
                                    logger.info("Product page listing={}", webPageEntity.getUrl());
                                    subscriber.onNext(webPageEntity);
                                }
                            }
                            subscriber.onCompleted();
                            return null;
                        }
                    })));
        });
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(200);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.firearmsoutletcanada.com/") && webPage.getType().equals("frontPage");
    }

}
