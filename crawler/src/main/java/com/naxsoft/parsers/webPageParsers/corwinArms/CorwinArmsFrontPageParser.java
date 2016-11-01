package com.naxsoft.parsers.webPageParsers.corwinArms;

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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class CorwinArmsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorwinArmsFrontPageParser.class);
    private static final Pattern pagerPattern = Pattern.compile("(\\d+) of (\\d+)");
    private final HttpClient client;

    private CorwinArmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#block-menu-menu-catalogue > div > ul a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");

                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", linkUrl, e.text());
                LOGGER.info("Found productList page {}", linkUrl);
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    private Observable<WebPageEntity> parseDocument2(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".pager li.pager-current");
            Matcher matcher = pagerPattern.matcher(elements.text());
            if (matcher.find()) {
                int max = Integer.parseInt(matcher.group(2));
                for (int i = 1; i <= max; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "?page=" + i, downloadResult.getSourcePage().getCategory());
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            } else {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity))
                .flatMap(this::parseDocument)
                .flatMap(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(this::parseDocument2);
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "corwin-arms.com";
    }


}
