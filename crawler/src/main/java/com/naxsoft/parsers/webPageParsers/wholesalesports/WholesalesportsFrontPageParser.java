package com.naxsoft.parsers.webPageParsers.wholesalesports;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class WholesalesportsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WholesalesportsFrontPageParser.class);
    private final HttpClient client;

    private WholesalesportsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            int max = 1;
            Elements elements = document.select(".pagination a");
            for (Element el : elements) {
                try {
                    int num = Integer.parseInt(el.text());
                    if (num > max) {
                        max = num;
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }

            for (int i = 0; i < max; i++) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "&page=" + i, downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearms/c/firearms?viewPageSize=72", "firearm"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Reloading/c/reloading?viewPageSize=72", "reload"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Ammunition/c/ammunition?viewPageSize=72", "ammi"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-and-Ammunition-Storage/c/Firearm%20and%20Ammunition%20Storage?viewPageSize=72", "misc"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Optics/c/optics?viewPageSize=72", "optic"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-Accessories/c/firearm-accessories?viewPageSize=72", "misc"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Range-Accessories/c/range-accessories?viewPageSize=72", "misc"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Hunting-Accessories/c/hunting-accessories?viewPageSize=72", "misc"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Air-Guns-%26-Slingshots/c/air-guns-slingshots?viewPageSize=72", "firearm"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Black-Powder/c/black-powder?viewPageSize=72", "firearm"));

        return Observable.from(webPageEntities)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("wholesalesports.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("wholesalesports.com/frontPage", (Message<WebPageEntity> event) -> {
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

