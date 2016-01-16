package com.naxsoft.parsers.webPageParsers.wholesalesports;

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
public class WholesalesportsFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(WholesalesportsFrontPageParser.class);
    private final HttpClient client;

    public WholesalesportsFrontPageParser(HttpClient client) {
        this.client = client;
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
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> {
            HashSet<WebPageEntity> webPageEntities = new HashSet<>();
            webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearms/c/firearms?viewPageSize=72"));
            webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Reloading/c/reloading?viewPageSize=72"));
            webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Ammunition/c/ammunition?viewPageSize=72"));
            webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-%26-Ammunition-Storage/c/Firearm%20%26%20Ammunition%20Storage?viewPageSize=72"));
            webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Optics/c/optics?viewPageSize=72"));
            webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-Accessories/c/firearm-accessories?viewPageSize=72"));
            webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Range-Accessories/c/range-accessories?viewPageSize=72"));
            webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Black-Powder/c/black-powder?viewPageSize=72"));

            for (WebPageEntity page : webPageEntities) {
                client.get(page.getUrl(), new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(Response resp) throws Exception {
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                            int max = 1;
                            Elements elements = document.select(".pagination a");
                            for (Element el : elements) {
                                try {
                                    int num = Integer.parseInt(el.text());
                                    if (num > max) {
                                        max = num;
                                    }
                                } catch (Exception ignored) {
                                    // ignore
                                }
                            }

                            for (int i = 0; i < max; i++) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(page.getUrl() + "&page=" + i);
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productList");
                                logger.info("Product page listing={}", webPageEntity.getUrl());
                                subscriber.onNext(webPageEntity);
                            }
                        }
                        subscriber.onCompleted();
                        return null;
                    }
                });
            }

        });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.wholesalesports.com/") && webPage.getType().equals("frontPage");
    }
}

