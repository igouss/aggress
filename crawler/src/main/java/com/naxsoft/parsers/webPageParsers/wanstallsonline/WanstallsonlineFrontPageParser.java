package com.naxsoft.parsers.webPageParsers.wanstallsonline;

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
import rx.Subscriber;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class WanstallsonlineFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    public WanstallsonlineFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WanstallsonlineFrontPageParser.class);

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.wanstallsonline.com/firearms/"));
        return Observable.create(subscriber -> Observable.from(webPageEntities).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new VoidCompletionHandler(page, subscriber)))));
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
        return webPage.getUrl().startsWith("http://www.wanstallsonline.com/") && webPage.getType().equals("frontPage");
    }

    private static class VoidCompletionHandler extends CompletionHandler<Void> {
        private final WebPageEntity page;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidCompletionHandler(WebPageEntity page, Subscriber<? super WebPageEntity> subscriber) {
            this.page = page;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), page.getUrl());
                parseDocument(document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
            int max = 1;
            Elements elements = document.select(".navigationtable td[valign=middle] > a");
            for (Element el : elements) {
                try {
                    Matcher matcher = Pattern.compile("\\d+").matcher(el.text());
                    if (matcher.find()) {
                        int num = Integer.parseInt(matcher.group());
                        if (num > max) {
                            max = num;
                        }
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }

            for (int i = 1; i < max; i++) {
                WebPageEntity webPageEntity = new WebPageEntity();
                if (1 == i) {
                    webPageEntity.setUrl(page.getUrl());
                } else {
                    webPageEntity.setUrl(page.getUrl() + "index " + i + ".html");
                }
                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                subscriber.onNext(webPageEntity);
            }
        }
    }
}

