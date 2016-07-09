package com.naxsoft.parsers.webPageParsers.leverarms;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
class LeverarmsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeverarmsFrontPageParser.class);
    private final HttpClient client;

    public LeverarmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#nav-sidebox li:not(.parent) a");

            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, e.attr("abs:href") + "?limit=all", e.text());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    private Collection<WebPageEntity> parseSubPages(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".item h4 a");

            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, e.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        Future<DownloadResult> future = client.get(parent.getUrl(), new DocumentCompletionHandler(parent));
        return Observable.from(future, Schedulers.io())
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(Observable::from)
                .flatMap(document -> Observable.from(parseSubPages(document)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.leverarms.com/") && webPage.getType().equals("frontPage");
    }
}