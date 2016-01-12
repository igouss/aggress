package com.naxsoft.parsers.webPageParsers.questar;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
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
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class QuestarFrontPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(QuestarFrontPageParser.class);

    public QuestarFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity parent) throws Exception {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearms/c/firearms?viewPageSize=72", parent));
        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Reloading/c/reloading?viewPageSize=72", parent));
        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Ammunition/c/ammunition?viewPageSize=72", parent));
        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-%26-Ammunition-Storage/c/Firearm%20%26%20Ammunition%20Storage?viewPageSize=72", parent));
        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Optics/c/optics?viewPageSize=72", parent));
        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-Accessories/c/firearm-accessories?viewPageSize=72", parent));
        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Range-Accessories/c/range-accessories?viewPageSize=72", parent));
        webPageEntities.add(create("http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Black-Powder/c/black-powder?viewPageSize=72", parent));
        return Observable.just(webPageEntities).
                flatMap(Observable::from).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new CompletionHandler<Set<WebPageEntity>>() {
                    @Override
                    public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                        HashSet<WebPageEntity> result = new HashSet<>();
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
                                result.add(webPageEntity);
                            }
                        }
                        return result;
                    }
                })));
    }

    private static WebPageEntity create(String url, WebPageEntity parent) {
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
        return webPage.getUrl().startsWith("http://www.wholesalesports.com/") && webPage.getType().equals("frontPage");
    }
}

