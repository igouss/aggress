package com.naxsoft.parsers.webPageParsers.wolverinesupplies;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

public class WolverinesuppliesFrontPageParser implements WebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(WolverinesuppliesFrontPageParser.class);

    public WolverinesuppliesFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select(".mainnav a");
                    for (Element e : elements) {
                        String linkUrl = e.attr("abs:href");
                        if (null != linkUrl && !linkUrl.isEmpty() && linkUrl.contains("Products") && e.siblingElements().isEmpty()) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(linkUrl);
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productList");
                            webPageEntity.setParent(webPage);
                            logger.info("ProductPageUrl={}, parseUrl={}", linkUrl, webPage.getUrl());
                            result.add(webPageEntity);
                        }
                    }
                }
                return result;
            }
        });
        return Observable.from(future);
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("https://www.wolverinesupplies.com/") && webPage.getType().equals("frontPage");
    }
}
