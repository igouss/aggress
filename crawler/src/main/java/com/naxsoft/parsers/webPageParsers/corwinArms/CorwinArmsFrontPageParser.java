package com.naxsoft.parsers.webPageParsers.corwinArms;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CorwinArmsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorwinArmsFrontPageParser.class);
    private final HttpClient client;

    public CorwinArmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Elements elements = document.select("#block-menu-menu-catalogue > div > ul a");
        for (Element e : elements) {
            String linkUrl = e.attr("abs:href");

            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(linkUrl);
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("Found on front page ={}", linkUrl);
            result.add(webPageEntity);
        }
        return result;
    }

    private Collection<WebPageEntity> parseDocument2(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".pager li.pager-current");
        Matcher matcher = Pattern.compile("(\\d+) of (\\d+)").matcher(elements.text());
        if (matcher.find()) {
            int max = Integer.parseInt(matcher.group(2));
            for (int i = 1; i <= max; i++) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(document.location() + "?page=" + i);
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        } else {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(document.location());
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        ListenableFuture<Document> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler());
        return Observable.from(future)
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler()))
                .flatMap(Observable::from)
                .flatMap(document -> Observable.from(parseDocument2(document)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("https://www.corwin-arms.com/") && webPage.getType().equals("frontPage");
    }
}
