package com.naxsoft.parsers.webPageParsers.dantesports;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class DantesportsProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsProductListParser.class);
    private static final Pattern producePagePattern = Pattern.compile("\\d+");
    private final HttpClient client;

    private DantesportsProductListParser(HttpClient client) {
        this.client = client;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#store div.listItem");

            for (Element element : elements) {
                String onclick = element.attr("onclick");
                Matcher matcher = producePagePattern.matcher(onclick);
                if (matcher.find()) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", "https://shop.dantesports.com/items_detail.php?iid=" + matcher.group(), downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                } else {
                    LOGGER.info("Product id not found: {}", document.location());
                }
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("dantesports.com") && webPage.getType().equals("productList");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("dantesports.com/productList", (Message<WebPageEntity> event) -> {
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
