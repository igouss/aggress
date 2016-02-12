package com.naxsoft.parsers.webPageParsers.questar;

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
public class QuestarFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestarFrontPageParser.class);
    private final HttpClient client;

    public QuestarFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("#catnav > li > a");

        for (Element e : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(e.attr("abs:href"));
            webPageEntity.setParsed(false);
            webPageEntity.setType("tmp");
            webPageEntity.setCategory(e.text());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    private Collection<WebPageEntity> parseSubPages(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>();

        Elements elements;
        // add subcatogories
        elements = document.select("#main > table > tbody > tr > td > p:nth-child(1) > strong > a");
        for (Element e : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(e.attr("abs:href"));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        // add product page
        elements = document.select("form > table > tbody > tr > td a");
        for (Element e : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(e.attr("abs:href"));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.from(client.get(parent.getUrl(), new DocumentCompletionHandler(parent)))
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseSubPages)
                .flatMap(Observable::from);

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shopquestar.com/") && webPage.getType().equals("frontPage");
    }
}

