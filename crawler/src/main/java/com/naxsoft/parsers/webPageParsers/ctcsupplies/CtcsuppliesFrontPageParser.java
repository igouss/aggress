package com.naxsoft.parsers.webPageParsers.ctcsupplies;

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
public class CtcsuppliesFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(CtcsuppliesFrontPageParser.class);

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
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
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl("http://ctcsupplies.ca/collections/all?page=" + i);
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("productList = {}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }


    public CtcsuppliesFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        ListenableFuture<DownloadResult> future = client.get("http://ctcsupplies.ca/collections/all", new DocumentCompletionHandler(parent));
        return Observable.from(future).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://ctcsupplies.ca/") && webPage.getType().equals("frontPage");
    }
}
