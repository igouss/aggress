package com.naxsoft.parsers.webPageParsers.bullseyelondon;

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

public class BullseyelondonFrontPageParser implements WebPageParser {
    private AsyncFetchClient client;

    public BullseyelondonFrontPageParser(AsyncFetchClient client) {

        this.client = client;
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) {

            Logger logger = LoggerFactory.getLogger(this.getClass());
            Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
                @Override
                public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                    HashSet<WebPageEntity> result = new HashSet<>();
                    if (resp.getStatusCode() == 200) {

                        Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                        Elements elements = document.select(".vertnav-cat a");
                        for (Element element : elements) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(element.attr("abs:href") + "?limit=all");
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productList");
                            webPageEntity.setParent(webPage);
                            logger.info("productList = " + webPageEntity.getUrl() + ", parent = " + webPage.getUrl());
                            result.add(webPageEntity);
                        }
                    }
                    return result;
                }
            });
        // return Observable.defer(() -> Observable.just(future.get()));
        return Observable.defer(() -> Observable.from(future));

    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("frontPage");
    }
}
