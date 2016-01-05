package com.naxsoft.parsers.webPageParsers.frontierfirearms;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
public class FrontierfirearmsFrontPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(FrontierfirearmsFrontPageParser.class);

    public FrontierfirearmsFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity parent) throws Exception {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://frontierfirearms.ca/firearms.html", parent));
        webPageEntities.add(create("http://frontierfirearms.ca/ammunition-reloading.html", parent));
        webPageEntities.add(create("http://frontierfirearms.ca/shooting-accessories.html", parent));
        webPageEntities.add(create("http://frontierfirearms.ca/optics.html", parent));
        return Observable.just(webPageEntities).
                flatMap(Observable::from).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new CompletionHandler<Set<WebPageEntity>>() {
                    @Override
                    public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                        HashSet<WebPageEntity> result = new HashSet<>();
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                            String elements = document.select("div.toolbar > div.pager > p").first().text();
                            Matcher matcher = Pattern.compile("of\\s(\\d+)").matcher(elements);
                            if (!matcher.find()) {
                                logger.error("Unable to parse total pages");
                                return result;
                            }

                            int productTotal = Integer.parseInt(matcher.group(1));
                            int pageTotal = (int) Math.ceil(productTotal / 30.0);

                            for (int i = 1; i <= pageTotal; i++) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(page.getUrl() + "?p=" + i);
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
        return webPage.getUrl().startsWith("http://frontierfirearms.ca/") && webPage.getType().equals("frontPage");
    }
}