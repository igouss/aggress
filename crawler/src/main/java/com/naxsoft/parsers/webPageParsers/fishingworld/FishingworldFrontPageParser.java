package com.naxsoft.parsers.webPageParsers.fishingworld;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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
class FishingworldFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FishingworldFrontPageParser.class);
    private final HttpClient client;

    public FishingworldFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, url, category);
        return webPageEntity;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("#list > div.bar.blue");
        Matcher matcher = Pattern.compile("(\\d+) of (\\d+)").matcher(elements.text());
        if (matcher.find()) {
            int max = Integer.parseInt(matcher.group(2));
            int postsPerPage = 10;
            int pages = (int) Math.ceil((double) max / postsPerPage);

            for (int i = 1; i <= pages; i++) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, document.location() + "?page=" + i, downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        } else {
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, document.location(), downloadResult.getSourcePage().getCategory());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://fishingworld.ca/hunting/66-guns", "firearm"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/67-ammunition", "ammo"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/66-guns", "firearm"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/146-optics", "optic"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/144-shooting-accesories", "misc"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/185-tree-stands", "misc"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/65-accessories", "misc"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/205-pellet-gun", "firearm"));

        return Observable.from(webPageEntities)
                .observeOn(Schedulers.io())
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://fishingworld.ca/") && webPage.getType().equals("frontPage");
    }
}