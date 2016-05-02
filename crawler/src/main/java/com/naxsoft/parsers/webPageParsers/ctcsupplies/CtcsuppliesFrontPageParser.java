package com.naxsoft.parsers.webPageParsers.ctcsupplies;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CtcsuppliesFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CtcsuppliesFrontPageParser.class);
    private final HttpClient client;

    public CtcsuppliesFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseCategories(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("nav:nth-child(2) > ul a");
        for (Element e : elements) {
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, e.attr("abs:href"), e.text());
            LOGGER.info("productList = {}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    private Collection<WebPageEntity> parseCategoryPages(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();
        Set<WebPageEntity> result = new HashSet<>(1);
        Elements elements = document.select("ul.pagination-custom  a");
        int max = 0;
        for (Element element : elements) {
            try {
                int tmp = Integer.parseInt(element.text());
                if (tmp > max) {
                    max = tmp;
                }
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        for (int i = 1; i <= max; i++) {
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, downloadResult.getSourcePage().getUrl() + "?page=" + i, downloadResult.getSourcePage().getCategory());
            LOGGER.info("productList = {}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.from(client.get(parent.getUrl(), new DocumentCompletionHandler(parent)))
                .map(this::parseCategories)
                .flatMap(Observable::from)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseCategoryPages)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://ctcsupplies.ca/") && webPage.getType().equals("frontPage");
    }
}
