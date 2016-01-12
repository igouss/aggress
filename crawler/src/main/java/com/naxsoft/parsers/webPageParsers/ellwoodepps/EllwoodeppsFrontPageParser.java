package com.naxsoft.parsers.webPageParsers.ellwoodepps;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
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
public class EllwoodeppsFrontPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(EllwoodeppsFrontPageParser.class);

    public EllwoodeppsFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://ellwoodepps.com/hunting/firearms.html?limit=100&no_cache=true", parent));

        return Observable.create(subscriber -> {
            Observable.just(webPageEntities).
                    flatMap(Observable::from).
                    flatMap(page -> Observable.from(client.get(page.getUrl(), new CompletionHandler<Void>() {
                        @Override
                        public Void onCompleted(Response resp) throws Exception {
                            if (200 == resp.getStatusCode()) {
                                Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                                String elements = document.select("div.amount-container > p").text();
                                Matcher matcher = Pattern.compile("of\\s(\\d+)").matcher(elements);
                                if (!matcher.find()) {
                                    logger.error("Unable to parse total pages");
                                    subscriber.onCompleted();
                                    return null;
                                }

                                int productTotal = Integer.parseInt(matcher.group(1));
                                int pageTotal = (int) Math.ceil(productTotal / 100.0);

                                for (int i = 1; i <= pageTotal; i++) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(page.getUrl() + "&p=" + i);
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("productList");
                                    logger.info("Product page listing={}", webPageEntity.getUrl());
                                    subscriber.onNext(webPageEntity);
                                }
                            }
                            subscriber.onCompleted();
                            return null;
                        }
                    })));
        });
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
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://ellwoodepps.com/") && webPage.getType().equals("frontPage");
    }
}