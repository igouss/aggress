package com.naxsoft.parsers.webPageParsers.frontierfirearms;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
public class FrontierfirearmsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierfirearmsFrontPageParser.class);

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        String elements = document.select("div.toolbar > div.pager > p").first().text();
        Matcher matcher = Pattern.compile("of\\s(\\d+)").matcher(elements);
        if (!matcher.find()) {
            LOGGER.error("Unable to parse total pages");
            return result;
        }

        int productTotal = Integer.parseInt(matcher.group(1));
        int pageTotal = (int) Math.ceil(productTotal / 30.0);

        for (int i = 1; i <= pageTotal; i++) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(document.location() + "?p=" + i);
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    public FrontierfirearmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://frontierfirearms.ca/firearms.html"));
        webPageEntities.add(create("http://frontierfirearms.ca/ammunition-reloading.html"));
        webPageEntities.add(create("http://frontierfirearms.ca/shooting-accessories.html"));
        webPageEntities.add(create("http://frontierfirearms.ca/optics.html"));
        return Observable.from(webPageEntities)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://frontierfirearms.ca/") && webPage.getType().equals("frontPage");
    }
}