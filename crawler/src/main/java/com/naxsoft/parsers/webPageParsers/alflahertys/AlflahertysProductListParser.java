package com.naxsoft.parsers.webPageParsers.alflahertys;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
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
public class AlflahertysProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlflahertysProductListParser.class);
    private final HttpClient client;

    public AlflahertysProductListParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Elements elements = document.select("body > div.container.main.content > div:nth-child(3) a:nth-child(1).view_product_info");

        for (Element element : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(element.attr("abs:href"));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            LOGGER.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), document.location());
            result.add(webPageEntity);
        }
        return result;
    }

    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        ListenableFuture<Document> future = client.get(parent.getUrl(), new DocumentCompletionHandler());
        return Observable.from(future).map(this::parseDocument).flatMap(Observable::from);
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("productList");
    }
}
