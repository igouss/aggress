package com.naxsoft.parsers.webPageParsers.gunshop;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class GunshopFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(GunshopFrontPageParser.class);

    private Collection<WebPageEntity> categoriesDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".menu-item > a");

        for (Element element : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(element.attr("abs:href"));
            if (!webPageEntity.getUrl().contains("product-category")) {
                continue;
            }
            webPageEntity.setParsed(false);
            webPageEntity.setType("tmp");
            webPageEntity.setCategory(element.text());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    private Collection<WebPageEntity> parseSubpages(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".woocommerce-result-count");
        Matcher matcher = Pattern.compile("(\\d+) of (\\d+)").matcher(elements.text());
        if (matcher.find()) {
            int max = Integer.parseInt(matcher.group(2));
            int postsPerPage = Integer.parseInt(matcher.group(1));
            int pages = (int) Math.ceil((double) max / postsPerPage);

            for (int i = 1; i <= pages; i++) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(document.location() + "/page/" + i + "/");
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        } else {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(document.location());
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    public GunshopFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        ListenableFuture<DownloadResult> future = client.get(parent.getUrl(), new DocumentCompletionHandler(parent));
        return Observable.from(future)
                .map(this::categoriesDocument)
                .flatMap(Observable::from)
                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(Observable::from)
                .flatMap(document -> Observable.from(parseSubpages(document)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://gun-shop.ca/") && webPage.getType().equals("frontPage");
    }
}