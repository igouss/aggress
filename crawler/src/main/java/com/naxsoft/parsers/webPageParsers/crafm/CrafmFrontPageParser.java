package com.naxsoft.parsers.webPageParsers.crafm;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.asynchttpclient.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

/**
 * Copyright NAXSoft 2015
 */
class CrafmFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmFrontPageParser.class);
    private final HttpClient client;

    private CrafmFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".nav-container a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", linkUrl, e.text());
                LOGGER.info("ProductPageUrl={}", linkUrl);
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        List<Cookie> cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store", "english", false, null, null, Long.MAX_VALUE, false, false));

        return client.get("http://www.crafm.com/catalogue/", cookies, new DocumentCompletionHandler(webPage))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("crafm.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("crafm.com/frontPage", (Message<WebPageEntity> event) -> {
            vertx.executeBlocking(future -> {
                Iterator<WebPageEntity> iterator = parse(event.body()).toBlocking().getIterator();
                future.complete(iterator);
            }, (AsyncResult<Iterator<WebPageEntity>> result) -> {
                if (result.succeeded()) {
                    Iterator<WebPageEntity> it = result.result();
                    while (it.hasNext()) {
                        vertx.eventBus().publish("webPageParseResult", it.next());
                    }
                } else {
                    LOGGER.error("Failed to parse", result.cause());
                }
            });
        });
    }
}
