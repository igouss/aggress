package com.naxsoft.parsers.webPageParsers.hical;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class HicalProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalProductListParser.class);

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);


        Elements elements;
        // Add sub categories
        if (!document.location().contains("page=")) {
            // add subpages
            elements = document.select("#CategoryPagingTop > div > ul > li > a");
            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(el.attr("abs:href"));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                webPageEntity.setCategory("n/a");
                LOGGER.info("ProductList sub-page {}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }

        elements = document.select("#frmCompare .ProductDetails a");
        for (Element el : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(el.attr("abs:href"));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    public HicalProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        ListenableFuture<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("productList");
    }
}