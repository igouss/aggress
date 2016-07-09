package com.naxsoft.parsers.webPageParsers.frontierfirearms;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
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
class FrontierfirearmsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierfirearmsFrontPageParser.class);
    private final HttpClient client;

    public FrontierfirearmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, url, category);
        return webPageEntity;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            String elements = document.select("div.toolbar > div.pager > p").first().text();
            Matcher matcher = Pattern.compile("of\\s(\\d+)").matcher(elements);
            if (!matcher.find()) {
                LOGGER.error("Unable to parse total pages");
                return result;
            }

            int productTotal = Integer.parseInt(matcher.group(1));
            int pageTotal = (int) Math.ceil(productTotal / 30.0);

            for (int i = 1; i <= pageTotal; i++) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, document.location() + "?p=" + i, downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://frontierfirearms.ca/firearms.html", "firearm"));
        webPageEntities.add(create("http://frontierfirearms.ca/ammunition-reloading.html", "ammo"));
        webPageEntities.add(create("http://frontierfirearms.ca/shooting-accessories.html", "misc"));
        webPageEntities.add(create("http://frontierfirearms.ca/optics.html", "optic"));
        return Observable.from(webPageEntities)
                .observeOn(Schedulers.io())
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://frontierfirearms.ca/") && webPage.getType().equals("frontPage");
    }
}