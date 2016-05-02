package com.naxsoft.parsers.webPageParsers.CanadaAmmo;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.ListenableFuture;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
public class CanadaAmmoFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadaAmmoFrontPageParser.class);
    private final HttpClient client;

    public CanadaAmmoFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseCategories(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        HashSet<WebPageEntity> result = new HashSet<>();
        Elements elements = document.select("ul#menu-main-menu:not(.off-canvas-list) > li > a");
        LOGGER.info("Parsing for sub-pages + {}", document.location());

        for (Element el : elements) {
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "tmp", false, el.attr("abs:href") + "?count=72", el.text());
            result.add(webPageEntity);
        }
        return result;
    }


    private Collection<WebPageEntity> parseCategoryPages(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        HashSet<WebPageEntity> subResult = new HashSet<>();
        Elements elements = document.select("div.clearfix span.pagination a.nav-page");
        if (elements.isEmpty()) {
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, document.location(), downloadResult.getSourcePage().getCategory());
            LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), document.location());
            subResult.add(webPageEntity);
        } else {
            int i = Integer.parseInt(elements.first().text()) - 1;
            int end = Integer.parseInt(elements.last().text());
            for (; i <= end; i++) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, document.location() + "&page=" + i, downloadResult.getSourcePage().getCategory());
                LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), document.location());
                subResult.add(webPageEntity);
            }
        }
        return subResult;
    }


    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        ListenableFuture<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future)
                .map(this::parseCategories)
                .flatMap(Observable::from)
                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(Observable::from)
                .flatMap(document -> Observable.from(parseCategoryPages(document)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("frontPage");
    }
}
