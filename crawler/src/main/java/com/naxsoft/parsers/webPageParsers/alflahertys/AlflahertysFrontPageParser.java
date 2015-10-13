package com.naxsoft.parsers.webPageParsers.alflahertys;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
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

/**
 * Copyright NAXSoft 2015
 */
public class AlflahertysFrontPageParser implements WebPageParser {
    private AsyncFetchClient client;

    public AlflahertysFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity parent) throws Exception {
        Future<Set<WebPageEntity>> future = client.get("http://www.alflahertys.com/collections/all/", new Handler(parent));
        return Observable.defer(() -> Observable.from(future));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("frontPage");
    }

    private class Handler extends AsyncCompletionHandler<Set<WebPageEntity>> {
        Logger logger;
        private WebPageEntity parent;

        public Handler(WebPageEntity parent) {
            this.parent = parent;
            logger = LoggerFactory.getLogger(this.getClass());
        }

        @Override
        public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
            HashSet<WebPageEntity> result = new HashSet<>();
            if (resp.getStatusCode() == 200) {
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
                    webPageEntity.setParent(parent);
                    logger.info("productList = " + webPageEntity.getUrl() + ", parent = " + parent.getUrl());
                    result.add(webPageEntity);
                }
            }
            return result;
        }
    }
}

