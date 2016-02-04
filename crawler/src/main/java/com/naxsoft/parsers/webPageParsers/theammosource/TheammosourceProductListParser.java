package com.naxsoft.parsers.webPageParsers.theammosource;

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
public class TheammosourceProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheammosourceProductListParser.class);
    private final HttpClient client;

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".categoryListBoxContents > a");
        if (!elements.isEmpty()) {
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(element.attr("abs:href"));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        } else {
            if (!document.location().contains("&page=")) {
                elements = document.select("#productsListingListingTopLinks > a");
                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(element.attr("abs:href"));
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productList");
                    LOGGER.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
            elements = document.select(".itemTitle a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(element.attr("abs:href"));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productPage");
                LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }


    public TheammosourceProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        ListenableFuture<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.theammosource.com/") && webPage.getType().equals("productList");
    }
}