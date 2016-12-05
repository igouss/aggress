package com.naxsoft.parsers.webPageParsers.leverarms;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.reactivex.Flowable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class LeverarmsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeverarmsFrontPageParser.class);

    public LeverarmsFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#nav-sidebox li:not(.parent) a");

            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", e.attr("abs:href") + "?limit=all", e.text());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result.build();
    }

    private Set<WebPageEntity> parseSubPages(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".item h4 a");

            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", e.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result.build();
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {

        return client.get(parent.getUrl(), new DocumentCompletionHandler(parent))
                .flatMapIterable(this::parseDocument)
                .flatMap(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMapIterable(this::parseSubPages)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "leverarms.com";
    }

}