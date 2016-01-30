package com.naxsoft.parsers.webPageParsers.dantesports;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
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
import java.util.LinkedList;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsFrontPageParser.class);
    private final HttpClient client;

    private Collection<WebPageEntity> parseDocument(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("#scol1 > div.scell_menu > li > a");

        for (Element element : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(element.attr("abs:href") + "&paging=0");
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("productList={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }


    public DantesportsFrontPageParser(HttpClient client) {
        this.client = client;
    }


    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(client.get("https://shop.dantesports.com/set_lang.php?lang=EN", new LinkedList<>(), getCookiesHandler(), false))
                .map(cookies -> client.get(webPage.getUrl(), cookies, new DocumentCompletionHandler()))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("frontPage");
    }
}
