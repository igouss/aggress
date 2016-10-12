package com.naxsoft.parsers.webPageParsers.marstar;

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

/**
 * Copyright NAXSoft 2015
 */
class MarstarProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarProductListParser.class);
    private final HttpClient client;

    private MarstarProductListParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity getProductList(WebPageEntity parent, Element e, String category) {
        String linkUrl = e.attr("abs:href") + "&displayOutOfStock=no";
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "productList", linkUrl, category);
        LOGGER.info("Found product list page {} url={}", e.text(), linkUrl);
        return webPageEntity;
    }

    private static WebPageEntity getProductPage(WebPageEntity parent, Element e, String category) {
        String linkUrl = e.attr("abs:href");
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "productPage", linkUrl, category);
        LOGGER.info("Found product {} url={}", e.text(), linkUrl);
        return webPageEntity;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            LOGGER.info("Parsing {}", document.select("h1").text());
            Elements elements = document.select("#main-content > div > table > tbody > tr > td > a:nth-child(3)");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductPage(downloadResult.getSourcePage(), e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
            elements = document.select(".baseTable td:nth-child(1) > a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductPage(downloadResult.getSourcePage(), e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
            elements = document.select("div.subcategoryName a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductList(downloadResult.getSourcePage(), e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("marstar.ca") && webPage.getType().equals("productList");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("marstar.ca/productList", getParseRequestMessageHandler());
    }
}
