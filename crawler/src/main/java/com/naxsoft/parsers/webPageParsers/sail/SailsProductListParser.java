package com.naxsoft.parsers.webPageParsers.sail;

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


class SailsProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SailsProductListParser.class);
    private static final Collection<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        cookies.add(new DefaultCookie("store_language", "english"));
    }

    public SailsProductListParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            // on first pass we don't specify p=1
            // add all subpages
            if (!document.location().contains("p=")) {
                Elements elements = document.select(".toolbar-bottom .pages a");
                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
            Elements elements = document.select(".item > a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
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
        return "sail.ca";
    }

}