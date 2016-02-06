package com.naxsoft.parsers.webPageParsers.firearmsoutletcanada;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.ListenableFuture;
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
public class FirearmsoutletcanadaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirearmsoutletcanadaFrontPageParser.class);
    private final HttpClient client;

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".products-grid .product-name > a");

        for (Element el : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(el.attr("abs:href"));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            webPageEntity.setCategory("n/a");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    public FirearmsoutletcanadaFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/pistols.html?limit=all&stock_status=64", "firearm"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/rifles.html?limit=all&stock_status=64", "firearm"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/shotguns.html?limit=all&stock_status=64", "firearm"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/ammo.html?limit=all&stock_status=64", "ammo"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/accessories.html?limit=all&stock_status=64", "misc"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/reloading.html?limit=all&stock_status=64", "reload"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/parts.html?limit=all&stock_status=64", "misc"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/sights-optics.html?limit=all&stock_status=64", "optic"));
        webPageEntities.add(create("http://www.firearmsoutletcanada.com/consignment.html?limit=all&stock_status=64", "firearm,optic"));

        return Observable.from(webPageEntities)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);


    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        webPageEntity.setCategory(category);
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.firearmsoutletcanada.com/") && webPage.getType().equals("frontPage");
    }
}
