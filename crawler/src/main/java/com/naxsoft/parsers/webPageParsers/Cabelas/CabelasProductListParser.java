package com.naxsoft.parsers.webPageParsers.cabelas;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CabelasProductListParser.class);
    private final HttpClient client;

    public CabelasProductListParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);
        if (isTerminalSubcategory(downloadResult)) {
            if (document.baseUri().contains("pagenumber")) {
                Elements elements = document.select(".productCard-heading a");
                for (Element element : elements) {
                    WebPageEntity webPageEntity = productPage(element.attr("abs:href"));
                    webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
                    result.add(webPageEntity);
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
                        WebPageEntity webPageEntity = getProductList(document.location() + "?pagenumber=" + i);
                        webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
                        result.add(webPageEntity);
                    }
                } else {
                    WebPageEntity webPageEntity = getProductList(document.location() + "?pagenumber=" + 1);
                    webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
                    result.add(webPageEntity);
                }
            }
        } else {
            Elements subPages = document.select("#categories > ul > li > a");
            for (Element element : subPages) {
                WebPageEntity subCategoryPage = getProductList( element.attr("abs:href"));
                if (downloadResult.getSourcePage().getCategory() == null) {
                    subCategoryPage.setCategory(element.text());
                } else {
                    subCategoryPage.setCategory(downloadResult.getSourcePage().getCategory());
                }
                result.add(subCategoryPage);
            }
        }
        return result;
    }


    private static WebPageEntity getProductList(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        LOGGER.info("productList={}", webPageEntity.getUrl());
        return webPageEntity;
    }

    private static boolean isTerminalSubcategory(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        return (1 == document.select(".categories .active").size()) || document.select("h1").text().equals("Thanks for visiting Cabelas.ca!");
    }

    private static WebPageEntity productPage(String url) {
        WebPageEntity productPage = new WebPageEntity();
        productPage.setUrl(url);
        productPage.setParsed(false);
        productPage.setType("productPage");
        return productPage;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        ListenableFuture<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future).map(this::parseDocument).flatMap(Observable::from);
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productList");
    }
}

