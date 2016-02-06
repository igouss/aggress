package com.naxsoft.parsers.webPageParsers.crafm;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.ListenableFuture;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CrafmProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmProductListParser.class);

    public CrafmProductListParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".products-grid .item > a");
        for (Element e : elements) {
            String linkUrl = e.attr("abs:href");
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(linkUrl);
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
            LOGGER.info("ProductPageUrl={}", linkUrl);
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        ListenableFuture<DownloadResult> future = client.get(webPage.getUrl(), new DocumentCompletionHandler(webPage));
        return Observable.from(future).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("http://www.crafm.com/") && webPage.getType().equals("productList");
    }

}

