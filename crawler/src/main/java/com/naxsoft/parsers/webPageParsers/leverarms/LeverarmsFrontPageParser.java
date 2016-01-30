package com.naxsoft.parsers.webPageParsers.leverarms;

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
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class LeverarmsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(LeverarmsFrontPageParser.class);

    private Collection<WebPageEntity> parseDocument(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".item h4 a");

        for (Element e : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(e.attr("abs:href"));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            webPageEntity.setCategory("n/a");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    public LeverarmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.leverarms.com/rifles.html?limit=all"));
        webPageEntities.add(create("http://www.leverarms.com/pistols.html?limit=all"));
        webPageEntities.add(create("http://www.leverarms.com/shotguns.html?limit=all"));
        webPageEntities.add(create("http://www.leverarms.com/ammo.html?limit=all"));
        webPageEntities.add(create("http://www.leverarms.com/accessories.html?limit=all"));
        webPageEntities.add(create("http://www.leverarms.com/surplus-firearms.html?limit=all"));
        webPageEntities.add(create("http://www.leverarms.com/used-firearms.html?limit=all"));
        return Observable.from(webPageEntities)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler()))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.leverarms.com/") && webPage.getType().equals("frontPage");
    }
}