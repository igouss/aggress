package com.naxsoft.parsers.webPageParsers.marstar;

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
public class MarstarProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarProductListParser.class);

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        LOGGER.info("Parsing {}", document.select("h1").text());
        Elements elements = document.select("#main-content > div > table > tbody > tr > td > a:nth-child(3)");
        for (Element e : elements) {
            WebPageEntity webPageEntity = getProductPage(e);
            result.add(webPageEntity);
        }
        elements = document.select(".baseTable td:nth-child(1) > a");
        for (Element e : elements) {
            WebPageEntity webPageEntity = getProductPage(e);
            result.add(webPageEntity);
        }
        elements = document.select("div.subcategoryName a");
        for (Element e : elements) {
            WebPageEntity webPageEntity = getProductList(e);
            result.add(webPageEntity);
        }
        return result;
    }

    private static WebPageEntity getProductList(Element e) {
        String linkUrl = e.attr("abs:href") + "&displayOutOfStock=no";
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(linkUrl);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        LOGGER.info("Found product list page {} url={}", e.text(), linkUrl);
        return webPageEntity;
    }

    private static WebPageEntity getProductPage(Element e) {
        String linkUrl = e.attr("abs:href");
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(linkUrl);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productPage");
        LOGGER.info("Found product {} url={}", e.text(), linkUrl);
        return webPageEntity;
    }

    public MarstarProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        ListenableFuture<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.marstar.ca/") && webPage.getType().equals("productList");
    }
}
