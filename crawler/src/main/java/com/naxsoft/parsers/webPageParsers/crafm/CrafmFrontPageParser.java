package com.naxsoft.parsers.webPageParsers.crafm;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.*;

/**
 * Copyright NAXSoft 2015
 */
public class CrafmFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmFrontPageParser.class);
    private final HttpClient client;

    public CrafmFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select(".nav-container a");
        for (Element e : elements) {
            String linkUrl = e.attr("abs:href");
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, linkUrl, e.text());
            LOGGER.info("ProductPageUrl={}", linkUrl);
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        List<Cookie> cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store", "english", false, null, null, Long.MAX_VALUE, false, false));

        ListenableFuture<DownloadResult> future = client.get("http://www.crafm.com/catalogue/", cookies, new DocumentCompletionHandler(webPage));
        return Observable.from(future, Schedulers.io()).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.crafm.com/") && webPage.getType().equals("frontPage");
    }
}
