package com.naxsoft.parsers.webPageParsers.internationalshootingsupplies;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.eventbus.Message;
import org.asynchttpclient.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class InternationalshootingsuppliesFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternationalshootingsuppliesFrontPageParser.class);
    private static final Collection<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store_language", "english", false, null, null, Long.MAX_VALUE, false, false));
    }

    private final HttpClient client;

    public InternationalshootingsuppliesFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "productList", false, url, category);
        return webPageEntity;
    }

    private Observable<WebPageEntity> parseProductPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".general-pagination a");
            int max = 0;
            for (Element element : elements) {
                try {
                    int tmp = Integer.parseInt(element.text());
                    if (tmp > max) {
                        max = tmp;
                    }
                } catch (NumberFormatException ignore) {
                    // ignore
                }
            }
            if (max == 0) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", false, downloadResult.getSourcePage().getUrl(), downloadResult.getSourcePage().getCategory());
                LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
                result.add(webPageEntity);
            } else {
                for (int i = 1; i <= max; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", false, downloadResult.getSourcePage().getUrl() + "page/" + i + "/", downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
                    result.add(webPageEntity);
                }
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/ammunition/", "ammo"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/firearms/", "firearm"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/hunting-accessories/", "misc"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/optics/", "optic"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/reloading-components/", "reload"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/reloading-equipment/", "reload"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/shooting-accessories/", "misc"));
        return Observable.from(webPageEntities)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseProductPage);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("internationalshootingsupplies.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("internationalshootingsupplies.com/frontPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}