package com.naxsoft.parsers.webPageParsers.hical;

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
public class HicalFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalFrontPageParser.class);

    private Collection<WebPageEntity> parseDocument(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements;

        // Add sub categories
        if (!document.location().contains("page=")) {
            elements = document.select(".SubCategoryList a");
            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(el.attr("abs:href"));
                webPageEntity.setParsed(false);
                webPageEntity.setType("frontPage");
                webPageEntity.setCategory("n/a");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
            // add subpages
            elements = document.select("#CategoryPagingTop > div > ul > li > a");
            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(el.attr("abs:href"));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                webPageEntity.setCategory("n/a");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        // add current page
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(document.location() + "?sort=featured&page=1");
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        webPageEntity.setCategory("n/a");
        LOGGER.info("Product page listing={}", webPageEntity.getUrl());
        result.add(webPageEntity);

        return result;
    }

    public HicalFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        if (parent.getUrl().equals("http://www.hical.ca/")) {
            webPageEntities.add(create("http://www.hical.ca/new-category/"));
            webPageEntities.add(create("http://www.hical.ca/firearm-accessories/"));
            webPageEntities.add(create("http://www.hical.ca/magazines/"));
            webPageEntities.add(create("http://www.hical.ca/stocks/"));
            webPageEntities.add(create("http://www.hical.ca/tools/"));
            webPageEntities.add(create("http://www.hical.ca/sights-optics/"));
            webPageEntities.add(create("http://www.hical.ca/soft-goods/"));
        } else {
            webPageEntities.add(parent);
        }

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
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("frontPage");
    }
}