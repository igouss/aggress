package com.naxsoft.parsers.webPageParsers.fishingworld;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class FishingworldFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FishingworldFrontPageParser.class);
    private final HttpClient client;

    public FishingworldFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
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
        return Observable.create(subscriber -> Observable.from(webPageEntities).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(Response resp) throws Exception {
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                            Elements elements = document.select("#list > div.bar.blue");
                            Matcher matcher = Pattern.compile("(\\d+) of (\\d+)").matcher(elements.text());
                            if (matcher.find()) {
                                int max = Integer.parseInt(matcher.group(2));
                                int postsPerPage = 10;
                                int pages = (int) Math.ceil((double) max / postsPerPage);

                                for (int i = 1; i <= pages; i++) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(page.getUrl() + "?page=" + i);
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setType("productList");
                                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                                    subscriber.onNext(webPageEntity);
                                }
                            } else {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(page.getUrl());
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setType("productList");
                                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                                subscriber.onNext(webPageEntity);
                            }
                        }
                        subscriber.onCompleted();
                        return null;
                    }
                }))));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://fishingworld.ca/") && webPage.getType().equals("frontPage");
    }
}