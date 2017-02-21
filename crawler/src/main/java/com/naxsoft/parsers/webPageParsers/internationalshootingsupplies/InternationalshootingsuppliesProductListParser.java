package com.naxsoft.parsers.webPageParsers.internationalshootingsupplies;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.DefaultCookie;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.asynchttpclient.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class InternationalshootingsuppliesProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternationalshootingsuppliesProductListParser.class);
    private static final Collection<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        cookies.add(new DefaultCookie("store_language", "english"));
    }

    public InternationalshootingsuppliesProductListParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Document document = downloadResult.getDocument();

        if (document != null) {
            Elements elements = document.select(".product-thumbnail");
            for (Element element : elements) {
                if (!element.select(".out-of-stock").isEmpty()) {
                    continue;
                }

                String url = element.select("> a").attr("abs:href");
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", url, downloadResult.getSourcePage().getCategory());
                LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity))
                .flatMap(this::parseDocument)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "internationalshootingsupplies.com";
    }


}