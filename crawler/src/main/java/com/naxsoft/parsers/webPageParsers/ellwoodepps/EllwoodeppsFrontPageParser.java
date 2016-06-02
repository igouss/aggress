package com.naxsoft.parsers.webPageParsers.ellwoodepps;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class EllwoodeppsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsFrontPageParser.class);
    private final HttpClient client;

    public EllwoodeppsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, url, category);
        return webPageEntity;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        String elements = document.select("#adj-nav-container > div.category-products > div.toolbar  div.amount-container > p").text();
        Matcher matcher = Pattern.compile("of\\s(\\d+)").matcher(elements);
        if (!matcher.find()) {
            LOGGER.error("Unable to parse total pages");
            return result;
        }

        int productTotal = Integer.parseInt(matcher.group(1));
        int pageTotal = (int) Math.ceil(productTotal / 100.0);

        for (int i = 1; i <= pageTotal; i++) {
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, document.location() + "&p=" + i, downloadResult.getSourcePage().getCategory());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://ellwoodepps.com/hunting/accessories.html?product_sold=3175", "accessories"));
        webPageEntities.add(create("https://ellwoodepps.com/hunting/firearms.html?product_sold=3175", "firearm"));

        return Observable.from(webPageEntities)
                .observeOn(Schedulers.io())
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://ellwoodepps.com/") && webPage.getType().equals("frontPage");
    }

}