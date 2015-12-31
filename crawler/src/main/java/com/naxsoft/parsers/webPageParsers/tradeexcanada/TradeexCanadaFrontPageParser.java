package com.naxsoft.parsers.webPageParsers.tradeexcanada;

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

/**
 * Copyright NAXSoft 2015
 */
public class TradeexCanadaFrontPageParser implements WebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(TradeexCanadaFrontPageParser.class);

    public TradeexCanadaFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity parent) throws Exception {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://www.tradeexcanada.com/products_list", parent));
        return Observable.just(webPageEntities).
                flatMap(Observable::from).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
                    @Override
                    public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                        HashSet<WebPageEntity> result = new HashSet<>();
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                            Elements elements = document.select(".view-content a");
                            for (Element element : elements) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(element.attr("abs:href"));
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productList");
                                webPageEntity.setParent(page.getParent());
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
        webPageEntity.setParent(parent);
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("frontPage");
    }
}