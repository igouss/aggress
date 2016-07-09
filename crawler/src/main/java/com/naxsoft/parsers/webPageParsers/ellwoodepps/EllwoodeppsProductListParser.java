package com.naxsoft.parsers.webPageParsers.ellwoodepps;

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
class EllwoodeppsProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsProductListParser.class);
    private final HttpClient client;

    public EllwoodeppsProductListParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".firearm-table");
            for (Element element : elements) {
                String category;
                if (downloadResult.getSourcePage().getCategory().equalsIgnoreCase("accessories")) {
                    category = element.select(".firearm-table > tbody > tr:nth-child(2) > td:nth-child(2)").text();
                } else {
                    category = downloadResult.getSourcePage().getCategory();
                }
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, element.select("td.firearm-name > a").attr("abs:href"), category);
                LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                result.add(webPageEntity);
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
        return webPage.getUrl().contains("ellwoodepps.com") && webPage.getType().equals("productList");
    }
}