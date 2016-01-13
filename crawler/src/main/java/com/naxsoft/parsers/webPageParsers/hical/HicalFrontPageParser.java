package com.naxsoft.parsers.webPageParsers.hical;

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

/**
 * Copyright NAXSoft 2015
 */
public class HicalFrontPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(HicalFrontPageParser.class);

    public HicalFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        if (parent.getUrl().equals("http://www.hical.ca/")) {
            webPageEntities.add(create("http://www.hical.ca/new-category/"));
            webPageEntities.add(create("http://www.hical.ca/firearm-accessories/"));
            webPageEntities.add(create("http://www.hical.ca/magazines/"));
            webPageEntities.add(create("http://www.hical.ca/stocks/"));
            webPageEntities.add(create("http://www.hical.ca/tools/"));
            webPageEntities.add(create("http://www.hical.ca/sights-optics/"));
            webPageEntities.add(create("http://www.hical.ca/soft-goods/"));
        } else {
            webPageEntities.add(parent);
        }

        return Observable.create(subscriber -> {
            Observable.from(webPageEntities).
                    flatMap(page -> Observable.from(client.get(page.getUrl(), new CompletionHandler<Void>() {
                        @Override
                        public Void onCompleted(Response resp) throws Exception {
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
                                        logger.info("Product page listing={}", webPageEntity.getUrl());
                                        subscriber.onNext(webPageEntity);
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
                                        logger.info("Product page listing={}", webPageEntity.getUrl());
                                        subscriber.onNext(webPageEntity);
                                    }
                                }
                                // add current page
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(resp.getUri().toString() + "?sort=featured&page=1");
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productList");
                                logger.info("Product page listing={}", webPageEntity.getUrl());
                                subscriber.onNext(webPageEntity);
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
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("frontPage");
    }
}