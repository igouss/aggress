package com.naxsoft.parsers.webPageParsers.questar;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
public class QuestarFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(QuestarFrontPageParser.class);
    private final HttpClient client;

    public QuestarFrontPageParser(HttpClient client) {
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
//        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
//        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearms/c/firearms?viewPageSize=72"));
//        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Reloading/c/reloading?viewPageSize=72"));
//        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Ammunition/c/ammunition?viewPageSize=72"));
//        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-%26-Ammunition-Storage/c/Firearm%20%26%20Ammunition%20Storage?viewPageSize=72"));
//        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Optics/c/optics?viewPageSize=72"));
//        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-Accessories/c/firearm-accessories?viewPageSize=72"));
//        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Range-Accessories/c/range-accessories?viewPageSize=72"));
//        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Black-Powder/c/black-powder?viewPageSize=72"));
//        return Observable.create(subscriber -> {
//            for (WebPageEntity e : webPageEntities) {
//                client.get(e.getUrl(), new CompletionHandler<Void>() {
//                    @Override
//                    public Void onCompleted(Response resp) throws Exception {
//                        if (200 == resp.getStatusCode()) {
//                            Document document = Jsoup.parse(resp.getResponseBody(), e.getUrl());
//                            int max = 1;
//                            Elements elements = document.select(".pagination a");
//                            for (Element el : elements) {
//                                try {
//                                    int num = Integer.parseInt(el.text());
//                                    if (num > max) {
//                                        max = num;
//                                    }
//                                } catch (Exception ignored) {
//                                    // ignore
//                                }
//                            }
//
//                            for (int i = 0; i < max; i++) {
//                                WebPageEntity webPageEntity = new WebPageEntity();
//                                webPageEntity.setUrl(e.getUrl() + "&page=" + i);
//                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
//                                webPageEntity.setParsed(false);
//                                webPageEntity.setStatusCode(resp.getStatusCode());
//                                webPageEntity.setType("productList");
//                                logger.info("Product page listing={}", webPageEntity.getUrl());
//                                subscriber.onNext(webPageEntity);
//                            }
//                        }
//                        subscriber.onCompleted();
//                        return null;
//                    }
//                });
//            }
//        });
        return Observable.empty();
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return false;
//        return webPage.getUrl().startsWith("https://shopquestar.com/") && webPage.getType().equals("frontPage");
    }
}

