package com.naxsoft.parsers.webPageParsers.tradeexcanada;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class TradeexCanadaFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaFrontPageParser.class);

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".view-content a");
        for (Element element : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(element.attr("abs:href"));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");

            if (element.text().contains("AAA Super Specials")) {
                webPageEntity.setCategory("firearms,ammo");
            }  else if (element.text().contains("Combination Guns")) {
                webPageEntity.setCategory("firearms");
            } else if (element.text().contains("Double Rifles")) {
                webPageEntity.setCategory("firearms");
            } else if (element.text().contains("Handguns")) {
                webPageEntity.setCategory("firearms");
            } else if (element.text().contains("Hunting and Sporting Arms")) {
                webPageEntity.setCategory("firearms");
            } else if (element.text().contains("Rifle")) {
                webPageEntity.setCategory("firearms");
            } else if (element.text().contains("Shotguns")) {
                webPageEntity.setCategory("firearms");
            } else if (element.text().contains("Ammunition")) {
                webPageEntity.setCategory("ammo");
            } else if (element.text().contains("Ammunition")) {
                webPageEntity.setCategory("Reloading Components");
            } else if (element.text().contains("Scopes")) {
                webPageEntity.setCategory("optics");
            } else {
                webPageEntity.setCategory("misc");
            }

            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    public TradeexCanadaFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://www.tradeexcanada.com/products_list"));

        return Observable.create(subscriber -> {
           for(WebPageEntity webPageEntity : webPageEntities) {
               client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
           }
        });
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        webPageEntity.setCategory("n/a");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("frontPage");
    }
}