package com.naxsoft.parsers.webPageParsers.ctcsupplies;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.reactivex.Observable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class CtcsuppliesFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CtcsuppliesFrontPageParser.class);

    public CtcsuppliesFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Observable<WebPageEntity> parseCategories(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();

        if (document != null) {
            Elements elements = document.select("nav:nth-child(2) > ul a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", e.attr("abs:href"), e.text());
                LOGGER.info("productList = {}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.fromIterable(result);
    }

    private Observable<WebPageEntity> parseCategoryPages(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
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
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", downloadResult.getSourcePage().getUrl() + "?page=" + i, downloadResult.getSourcePage().getCategory());
                LOGGER.info("productList = {}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.fromIterable(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return client.get(parent.getUrl(), new DocumentCompletionHandler(parent))
                .flatMap(this::parseCategories)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseCategoryPages)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "ctcsupplies.ca";
    }

}
