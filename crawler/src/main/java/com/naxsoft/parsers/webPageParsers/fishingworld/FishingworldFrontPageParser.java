package com.naxsoft.parsers.webPageParsers.fishingworld;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class FishingworldFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FishingworldFrontPageParser.class);
    private final HttpClient client;
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
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(document.location() + "?page=" + i);
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        } else {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(document.location());
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }


    public FishingworldFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://fishingworld.ca/hunting/66-guns"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/67-ammunition"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/66-guns"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/146-optics"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/144-shooting-accesories"));
        webPageEntities.add(create("https://fishingworld.ca/hunting/185-tree-stands"));

        return Observable.from(webPageEntities)
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