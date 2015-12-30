package com.naxsoft.parsers.webPageParsers.hical;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
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

/**
 * Copyright NAXSoft 2015
 */
public class HicalFrontPageParser implements WebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(HicalFrontPageParser.class);

    public HicalFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity parent) throws Exception {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        if (parent.getUrl().equals("http://www.hical.ca/")) {
            webPageEntities.add(create("http://www.hical.ca/new-category/", parent));
            webPageEntities.add(create("http://www.hical.ca/firearm-accessories/", parent));
            webPageEntities.add(create("http://www.hical.ca/magazines/", parent));
            webPageEntities.add(create("http://www.hical.ca/stocks/", parent));
            webPageEntities.add(create("http://www.hical.ca/tools/", parent));
            webPageEntities.add(create("http://www.hical.ca/sights-optics/", parent));
            webPageEntities.add(create("http://www.hical.ca/soft-goods/", parent));
        } else {
            webPageEntities.add(parent);
        }
        return Observable.just(webPageEntities).
                flatMap(Observable::from).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
                    @Override
                    public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                        HashSet<WebPageEntity> result = new HashSet<>();
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                            Elements elements;

                            // Add sub categories
                            if (!resp.getUri().toString().contains("page=")) {
                                elements = document.select(".SubCategoryList a");
                                for (Element el : elements) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(el.attr("abs:href"));
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("frontPage");
                                    webPageEntity.setParent(parent);
                                    logger.info("Product page listing={}", webPageEntity.getUrl());
                                    result.add(webPageEntity);
                                }
                                // add subpages
                                elements = document.select("#CategoryPagingTop > div > ul > li > a");
                                for (Element el : elements) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(el.attr("abs:href"));
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("productList");
                                    webPageEntity.setParent(parent);
                                    logger.info("Product page listing={}", webPageEntity.getUrl());
                                    result.add(webPageEntity);
                                }
                            }
                            // add current page
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(resp.getUri().toString() + "?sort=featured&page=1");
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productList");
                            webPageEntity.setParent(parent);
                            logger.info("Product page listing={}", webPageEntity.getUrl());
                            result.add(webPageEntity);
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
        webPageEntity.setParent(parent);
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("frontPage");
    }
}