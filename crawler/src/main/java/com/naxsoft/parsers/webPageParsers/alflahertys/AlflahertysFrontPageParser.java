package com.naxsoft.parsers.webPageParsers.alflahertys;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
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
public class AlflahertysFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(AlflahertysFrontPageParser.class);

    private final HttpClient client;

    public AlflahertysFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> {
            client.get("http://www.alflahertys.com/collections/all/", new CompletionHandler<Void>() {
                @Override
                public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                    if (200 == resp.getStatusCode()) {
                        Document document = Jsoup.parse(resp.getResponseBody(), resp.getUri().toString());
                        Elements elements = document.select(".paginate a");
                        int max = 0;
                        for (Element element : elements) {
                            try {
                                int tmp = Integer.parseInt(element.text());
                                if (tmp > max) {
                                    max = tmp;
                                }
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                        for (int i = 1; i <= max; i++) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl("http://www.alflahertys.com/collections/all?page=" + i);
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productList");
                            logger.info("productList = {}, parent = {}", webPageEntity.getUrl(), resp.getUri());
                            subscriber.onNext(webPageEntity);
                        }
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("frontPage");
    }

}

