package com.naxsoft.parsers.webPageParsers.gunshop;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class GunshopFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GunshopFrontPageParser.class);
    private static final Pattern maxPagesPattern = Pattern.compile("(\\d+) of (\\d+)");

    public GunshopFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Observable<WebPageEntity> categoriesDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".menu-item > a");

            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "tmp", element.attr("abs:href"), element.text());
                if (!webPageEntity.getUrl().contains("product-category")) {
                    continue;
                }
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.fromIterable(result);
    }

    private Observable<WebPageEntity> parseSubpages(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".woocommerce-result-count");
            Matcher matcher = maxPagesPattern.matcher(elements.text());
            if (matcher.find()) {
                int max = Integer.parseInt(matcher.group(2));
                int postsPerPage = Integer.parseInt(matcher.group(1));
                int pages = (int) Math.ceil((double) max / postsPerPage);

                for (int i = 1; i <= pages; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "/page/" + i + "/", downloadResult.getSourcePage().getCategory());
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            } else {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.fromIterable(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {

        return client.get(parent.getUrl(), new DocumentCompletionHandler(parent))
                .flatMap(this::categoriesDocument)
                .flatMap(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(this::parseSubpages)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "gun-shop.ca";
    }


}