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
import rx.Subscriber;

import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CabelasProductListParser.class);
    private final HttpClient client;

    public CabelasProductListParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity getProductList(WebPageEntity parent, String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        webPageEntity.setCategory(parent.getCategory());
        LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), parent.getUrl());
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
        productPage.setType("productPage");
        return productPage;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> client.get(webPage.getUrl(), new VoidCompletionHandler(webPage, subscriber)));
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productList");
    }

    private static class VoidCompletionHandler extends CompletionHandler<Void> {
        private final WebPageEntity webPage;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidCompletionHandler(WebPageEntity webPage, Subscriber<? super WebPageEntity> subscriber) {
            this.webPage = webPage;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                webPage.setParsed(true);
                Document document = Jsoup.parse(response.getResponseBody(), webPage.getUrl());
                parseDocument(document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
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
                            subscriber.onNext(getProductList(webPage, webPage.getUrl() + "?pagenumber=" + i));
                        }
                    } else {
                        subscriber.onNext(getProductList(webPage, webPage.getUrl() + "?pagenumber=" + 1));
                    }
                }
            } else {
                Elements subPages = document.select("#categories > ul > li > a");
                for (Element element : subPages) {
                    WebPageEntity subCategoryPage = getProductList(webPage, element.attr("abs:href"));
                    subscriber.onNext(subCategoryPage);
                }
            }
        }
    }
}

