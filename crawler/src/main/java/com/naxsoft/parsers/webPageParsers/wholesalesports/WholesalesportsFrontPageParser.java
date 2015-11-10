package com.naxsoft.parsers.webPageParsers.wholesalesports;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.parsers.webPageParsers.westrifle.WestrifleProductPageParser;
import com.ning.http.client.AsyncCompletionHandler;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class WholesalesportsFrontPageParser implements WebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(WholesalesportsFrontPageParser.class);

    public WholesalesportsFrontPageParser(AsyncFetchClient client) {
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
        return Observable.defer(() -> Observable.just(webPageEntities).
                        flatMap(Observable::from).
                        flatMap(page -> Observable.from(client.get(page.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
                            @Override
                            public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                                HashSet<WebPageEntity> result = new HashSet<>();
                                if (resp.getStatusCode() == 200) {
                                    Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                                    int max = 1;
                                    Elements elements = document.select(".pagination a");
                                    for(Element el : elements) {
                                        try {
                                            int num = Integer.parseInt(el.text());
                                            if (num > max) {
                                                max = num;
                                            }
                                        } catch (Exception e) {
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
                                        webPageEntity.setParent(page.getParent());
                                        logger.info("Product page listing=" + webPageEntity.getUrl());
                                        result.add(webPageEntity);
                                    }
                                }
                                return result;
                            }
                        }))));
    }

    private WebPageEntity create(String url, WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(200);
        webPageEntity.setType("productList");
        webPageEntity.setParent(parent);
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.wholesalesports.com/") && webPage.getType().equals("frontPage");
    }
}

