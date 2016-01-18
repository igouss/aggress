package com.naxsoft.parsers.webPageParsers.ctcsupplies;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
public class CtcsuppliesFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(CtcsuppliesFrontPageParser.class);

    public CtcsuppliesFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> client.get("http://ctcsupplies.ca/collections/all", new CompletionHandler<Void>() {
            @Override
            public Void onCompleted(Response resp) throws Exception {
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), resp.getUri().toString());
                    Elements elements = document.select("ul.pagination-custom  a");
                    int max = 0;
                    for (Element element : elements) {
                        try {
                            int tmp = Integer.parseInt(element.text());
                            if (tmp > max) {
                                max = tmp;
                            }
                        } catch (NumberFormatException ignored) {
                            // ignore
                        }
                    }
                    for (int i = 1; i <= max; i++) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl("http://ctcsupplies.ca/collections/all?page=" + i);
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setType("productList");
                        LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), resp.getUri());
                        subscriber.onNext(webPageEntity);
                    }
                }
                subscriber.onCompleted();
                return null;
            }
        }));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://ctcsupplies.ca/") && webPage.getType().equals("frontPage");
    }


}

