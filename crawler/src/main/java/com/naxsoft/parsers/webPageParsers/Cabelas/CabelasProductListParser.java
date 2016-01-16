package com.naxsoft.parsers.webPageParsers.cabelas;

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

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductListParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(CabelasProductListParser.class);
    private final HttpClient client;

    public CabelasProductListParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity getProductList(WebPageEntity parent, int statusCode, String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(statusCode);
        webPageEntity.setType("productList");
        webPageEntity.setCategory(parent.getCategory());
        logger.info("productList=" + webPageEntity.getUrl() + ", parent=" + parent.getUrl());
        return webPageEntity;
    }

    private static boolean isTerminalSubcategory(Document document) {
        return (1 == document.select(".categories .active").size()) || document.select("h1").text().equals("Thanks for visiting Cabelas.ca!");
    }

    private static WebPageEntity productPage(int statusCode, String url) {
        WebPageEntity productPage = new WebPageEntity();
        productPage.setUrl(url);
        productPage.setModificationDate(new Timestamp(System.currentTimeMillis()));
        productPage.setParsed(false);
        productPage.setStatusCode(statusCode);
        productPage.setType("productPage");
        return productPage;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> {
            client.get(webPage.getUrl(), new CompletionHandler<Void>() {
                @Override
                public Void onCompleted(Response resp) throws Exception {
                    if (200 == resp.getStatusCode()) {
                        webPage.setParsed(true);
                        Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                        if (isTerminalSubcategory(document)) {
                            if (document.baseUri().contains("pagenumber")) {
                                Elements elements = document.select(".productCard-heading a");
                                for (Element element : elements) {
                                    subscriber.onNext(productPage(200, element.attr("abs:href")));
                                }
                            } else {
                                Elements subPages = document.select("#main footer > nav span, #main footer > nav a");
                                if (!subPages.isEmpty()) {
                                    int max = 1;
                                    for (Element subpage : subPages) {
                                        try {
                                            int page = Integer.parseInt(subpage.text());
                                            if (page > max) {
                                                max = page;
                                            }
                                        } catch (Exception ignored) {
                                            // ignore
                                        }
                                    }
                                    for (int i = 1; i <= max; i++) {
                                        subscriber.onNext(getProductList(webPage, 200, webPage.getUrl() + "?pagenumber=" + i));
                                    }
                                } else {
                                    subscriber.onNext(getProductList(webPage, 200, webPage.getUrl() + "?pagenumber=" + 1));
                                }
                            }
                        } else {
                            Elements subPages = document.select("#categories > ul > li > a");
                            for (Element element : subPages) {
                                WebPageEntity subCategoryPage = getProductList(webPage, 200, element.attr("abs:href"));
                                subscriber.onNext(subCategoryPage);
                            }
                        }
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productList");
    }
}

