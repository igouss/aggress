package com.naxsoft.parsers.webPageParsers.marstar;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.eventbus.Message;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
class MarstarProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarProductListParser.class);
    private final HttpClient client;

    public MarstarProductListParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity getProductList(Element e, String category) {
        String linkUrl = e.attr("abs:href") + "&displayOutOfStock=no";
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, linkUrl, category);
        LOGGER.info("Found product list page {} url={}", e.text(), linkUrl);
        return webPageEntity;
    }

    private static WebPageEntity getProductPage(Element e, String category) {
        String linkUrl = e.attr("abs:href");
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, linkUrl, category);
        LOGGER.info("Found product {} url={}", e.text(), linkUrl);
        return webPageEntity;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            LOGGER.info("Parsing {}", document.select("h1").text());
            Elements elements = document.select("#main-content > div > table > tbody > tr > td > a:nth-child(3)");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductPage(e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
            elements = document.select(".baseTable td:nth-child(1) > a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductPage(e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
            elements = document.select("div.subcategoryName a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductList(e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        Future<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future, Schedulers.io()).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("marstar.ca") && webPage.getType().equals("productList");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("marstar.ca/productList", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
