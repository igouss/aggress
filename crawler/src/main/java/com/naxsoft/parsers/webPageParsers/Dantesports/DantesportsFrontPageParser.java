package com.naxsoft.parsers.webPageParsers.Dantesports;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsFrontPageParser.class);
    private final HttpClient client;

    public DantesportsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("#scol1 > div.scell_menu > li > a");

        for (Element element : elements) {
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, element.attr("abs:href") + "&paging=0", element.text());
            LOGGER.info("productList={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        List<Cookie> cookies = null;
        try {
            cookies = client.get("https://shop.dantesports.com/set_lang.php?lang=EN", new LinkedList<>(), getCookiesHandler(), false).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return Observable.from(client.get(webPage.getUrl(), cookies, new DocumentCompletionHandler(webPage)))
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("frontPage");
    }
}
