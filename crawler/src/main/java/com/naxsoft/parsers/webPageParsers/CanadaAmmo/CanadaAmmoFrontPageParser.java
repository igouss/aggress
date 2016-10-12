package com.naxsoft.parsers.webPageParsers.canadaAmmo;

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

/**
 * Copyright NAXSoft 2015
 */
class CanadaAmmoFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadaAmmoFrontPageParser.class);
    private final HttpClient client;

    private CanadaAmmoFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Observable<WebPageEntity> parseCategories(DownloadResult downloadResult) {
        HashSet<WebPageEntity> result = new HashSet<>();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("ul#menu-main-menu:not(.off-canvas-list) > li > a");
            LOGGER.info("Parsing for sub-pages + {}", document.location());

            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "tmp", el.attr("abs:href") + "?count=72", el.text());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }


    private Observable<WebPageEntity> parseCategoryPages(DownloadResult downloadResult) {
        HashSet<WebPageEntity> subResult = new HashSet<>();

        Document document = downloadResult.getDocument();
        if (document != null) {

            Elements elements = document.select("div.clearfix span.pagination a.nav-page");
            if (elements.isEmpty()) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), document.location());
                subResult.add(webPageEntity);
            } else {
                int i = Integer.parseInt(elements.first().text()) - 1;
                int end = Integer.parseInt(elements.last().text());
                for (; i <= end; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "&page=" + i, downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), document.location());
                    subResult.add(webPageEntity);
                }
            }
        }
        return Observable.from(subResult);
    }


    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity))
                .flatMap(this::parseCategories)
                .flatMap(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(this::parseCategoryPages);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("canadaammo.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("canadaammo.com/frontPage", getParseRequestMessageHandler());
    }
}
