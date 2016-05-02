package com.naxsoft.parsers.webPageParsers.tradeexcanada;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class TradeexCanadaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaFrontPageParser.class);
    private final HttpClient client;

    public TradeexCanadaFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, url, "N/A");
        return webPageEntity;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".view-content a");
        for (Element element : elements) {
            String category;
            if (element.text().contains("AAA Super Specials")) {
                category = "firearm,ammo";
            } else if (element.text().contains("Combination Guns")) {
                category = "firearm";
            } else if (element.text().contains("Double Rifles")) {
                category = "firearm";
            } else if (element.text().contains("Handguns")) {
                category = "firearm";
            } else if (element.text().contains("Hunting and Sporting Arms")) {
                category = "firearm";
            } else if (element.text().contains("Rifle")) {
                category = "firearm";
            } else if (element.text().contains("Shotguns")) {
                category = "firearm";
            } else if (element.text().contains("Ammunition")) {
                category = "ammo";
            } else if (element.text().contains("Reloading Components")) {
                category = "reload";
            } else if (element.text().contains("Scopes")) {
                category = "optic";
            } else {
                category = "misc";
            }

            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, element.attr("abs:href"), category);

            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://www.tradeexcanada.com/products_list"));

        return Observable.from(webPageEntities)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("frontPage");
    }
}