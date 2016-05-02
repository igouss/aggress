package com.naxsoft.parsers.webPageParsers.westrifle;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.ListenableFuture;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class WestrifleFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleFrontPageParser.class);
    private final HttpClient client;

    public WestrifleFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".leftBoxContainer .category-top");
        for(Element e : elements) {
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "tmp", false, e.attr("abs:href"), e.text());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    private Collection<WebPageEntity> parseDocument2(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>();

        Elements elements = document.select("#productsListingTopNumber > strong:nth-child(3)");
        int productTotal = Integer.parseInt(elements.text());
        int pageTotal = (int) Math.ceil(productTotal / 10.0);

        for (int i = 1; i <= pageTotal; i++) {
            WebPageEntity webPageEntity = new WebPageEntity(0l, "", "productList", false, document.location() + "&page=" + i, downloadResult.getSourcePage().getCategory());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        ListenableFuture<DownloadResult> future = client.get(parent.getUrl(), new DocumentCompletionHandler(parent));
        return Observable.from(future)
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(Observable::from)
                .flatMap(document -> Observable.from(parseDocument2(document)));
    }


    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://westrifle.com/") && webPage.getType().equals("frontPage");
    }
}