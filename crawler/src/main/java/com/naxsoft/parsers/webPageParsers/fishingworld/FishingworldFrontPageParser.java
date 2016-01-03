package com.naxsoft.parsers.webPageParsers.fishingworld;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class FishingworldFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(FishingworldFrontPageParser.class);
    private final AsyncFetchClient client;

    public FishingworldFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url, WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(200);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity parent) throws Exception {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://fishingworld.ca/hunting/66-guns", parent));
        webPageEntities.add(create("https://fishingworld.ca/hunting/67-ammunition", parent));
        webPageEntities.add(create("https://fishingworld.ca/hunting/66-guns", parent));
        webPageEntities.add(create("https://fishingworld.ca/hunting/146-optics", parent));
        webPageEntities.add(create("https://fishingworld.ca/hunting/144-shooting-accesories", parent));
        webPageEntities.add(create("https://fishingworld.ca/hunting/185-tree-stands", parent));
        return Observable.just(webPageEntities).
                flatMap(Observable::from).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
                    @Override
                    public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                        HashSet<WebPageEntity> result = new HashSet<>();
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
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("productList");
                                    logger.info("Product page listing={}", webPageEntity.getUrl());
                                    result.add(webPageEntity);
                                }
                            } else {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(page.getUrl());
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productList");
                                logger.info("Product page listing={}", webPageEntity.getUrl());
                                result.add(webPageEntity);
                            }
                        }
                        return result;
                    }
                })));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://fishingworld.ca/") && webPage.getType().equals("frontPage");
    }
}