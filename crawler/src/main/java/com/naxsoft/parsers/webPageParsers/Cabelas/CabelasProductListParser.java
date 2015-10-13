package com.naxsoft.parsers.webPageParsers.Cabelas;

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
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductListParser implements WebPageParser {
    private final AsyncFetchClient client;
    private final Logger logger;

    public CabelasProductListParser(AsyncFetchClient client) {
        this.client = client;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {

        Future<Set<Document>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<Document>>() {
            @Override
            public Set<Document> onCompleted(Response resp) throws Exception {
                HashSet<Document> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    webPage.setParsed(true);
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    result.add(document);
                }
                return result;
            }
        });
        return Observable.from(future).map(documents -> {
            HashSet<WebPageEntity> result = new HashSet<>();
            for (Document document : documents) {
                if (isTerminalSubcategory(document)) {
                    if (document.baseUri().contains("pagenumber")) {
                        Elements elements = document.select(".productCard-heading a");
                        for (Element element : elements) {
                            result.add(productPage(webPage.getParent(), 200, element.attr("abs:href")));
                        }
                    } else {
                        Elements subPages = document.select("#main footer > nav span, #main footer > nav a");
                        if (subPages.size() != 0) {
                            int max = 1;
                            for (Element subpage : subPages) {
                                try {
                                    int page = Integer.parseInt(subpage.text());
                                    if (page > max) {
                                        max = page;
                                    }
                                } catch (Exception e) {
                                    // ignore
                                }
                            }
                            for (int i = 1; i <= max; i++) {
                                result.add(getProductList(webPage, 200, webPage.getUrl() + "?pagenumber=" + i));
                            }
                        } else {
                            result.add(getProductList(webPage, 200, webPage.getUrl() + "?pagenumber=" + 1));
                        }
                    }
                } else {
                    Elements subPages = document.select("#categories > ul > li > a");
                    for (Element element : subPages) {
                        WebPageEntity subCategoryPage = getProductList(webPage, 200, element.attr("abs:href"));
                        result.add(subCategoryPage);
                    }
                }
            }
            return result;
        });
    }

    private WebPageEntity getProductList(WebPageEntity parent, int statusCode, String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(statusCode);
        webPageEntity.setParent(parent);
        webPageEntity.setType("productList");
        logger.info("productList=" + webPageEntity.getUrl() + ", parent=" + parent.getUrl());
        return webPageEntity;
    }


    private boolean isTerminalSubcategory(Document document) {
        return (document.select(".categories .active").size() == 1) || document.select("h1").text().equals("Thanks for visiting Cabelas.ca!");
    }

    private WebPageEntity productPage(WebPageEntity parent, int statusCode, String url) {
        WebPageEntity productPage = new WebPageEntity();
        productPage.setUrl(url);
        productPage.setModificationDate(new Timestamp(System.currentTimeMillis()));
        productPage.setParsed(false);
        productPage.setStatusCode(statusCode);
        productPage.setType("productPage");
        productPage.setParent(parent);
        return productPage;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productList");
    }
}

