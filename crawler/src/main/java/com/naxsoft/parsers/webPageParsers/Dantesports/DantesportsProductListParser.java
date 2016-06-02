package com.naxsoft.parsers.webPageParsers.Dantesports;

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
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsProductListParser.class);
    private final HttpClient client;

    public DantesportsProductListParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("#store div.listItem");

        for (Element element : elements) {
            String onclick = element.attr("onclick");
            Matcher matcher = Pattern.compile("\\d+").matcher(onclick);
            if (matcher.find()) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, "https://shop.dantesports.com/items_detail.php?iid=" + matcher.group(), downloadResult.getSourcePage().getCategory());
                LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            } else {
                LOGGER.info("Product id not found: {}", document.location());
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        ListenableFuture<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future, Schedulers.io()).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("productList");
    }
}
