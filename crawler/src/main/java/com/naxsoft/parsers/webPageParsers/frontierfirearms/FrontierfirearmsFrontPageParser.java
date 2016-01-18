package com.naxsoft.parsers.webPageParsers.frontierfirearms;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
public class FrontierfirearmsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierfirearmsFrontPageParser.class);

    public FrontierfirearmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://frontierfirearms.ca/firearms.html"));
        webPageEntities.add(create("http://frontierfirearms.ca/ammunition-reloading.html"));
        webPageEntities.add(create("http://frontierfirearms.ca/shooting-accessories.html"));
        webPageEntities.add(create("http://frontierfirearms.ca/optics.html"));
        return Observable.create(subscriber -> Observable.from(webPageEntities).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(Response resp) throws Exception {
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                            String elements = document.select("div.toolbar > div.pager > p").first().text();
                            Matcher matcher = Pattern.compile("of\\s(\\d+)").matcher(elements);
                            if (!matcher.find()) {
                                LOGGER.error("Unable to parse total pages");
                                subscriber.onCompleted();
                                return null;
                            }

                            int productTotal = Integer.parseInt(matcher.group(1));
                            int pageTotal = (int) Math.ceil(productTotal / 30.0);

                            for (int i = 1; i <= pageTotal; i++) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(page.getUrl() + "?p=" + i);
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

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://frontierfirearms.ca/") && webPage.getType().equals("frontPage");
    }
}