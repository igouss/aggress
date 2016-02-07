package com.naxsoft.parsers.webPageParsers.gunshop;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

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
    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
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
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        } else {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(document.location());
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    public GunshopFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://gun-shop.ca/browse/"));

        return Observable.from(webPageEntities)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://gun-shop.ca/") && webPage.getType().equals("frontPage");
    }
}