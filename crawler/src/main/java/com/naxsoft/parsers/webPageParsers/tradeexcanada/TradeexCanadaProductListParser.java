package com.naxsoft.parsers.webPageParsers.tradeexcanada;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
class TradeexCanadaProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaProductListParser.class);
    private final HttpClient client;

    public TradeexCanadaProductListParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url) {
        return new WebPageEntity(0L, "", "productList", false, url, "");
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            if (document.location().contains("page=")) {
                Elements elements = document.select(".view-content a");
                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, element.attr("abs:href"), "");
                    LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            } else {
                Elements subPages = document.select(".pager a");
                for (Element subPage : subPages) {
                    result.add(create(subPage.attr("abs:href")));
                }
                result.add(create(document.location() + "?page=0"));
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        Future<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future, Schedulers.io()).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("productList");
    }
}