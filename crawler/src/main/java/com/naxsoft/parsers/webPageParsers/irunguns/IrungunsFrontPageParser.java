package com.naxsoft.parsers.webPageParsers.irunguns;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.eventbus.Message;
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
class IrungunsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(IrungunsFrontPageParser.class);
    private final HttpClient client;

    public IrungunsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();

        if (document != null) {
            Elements elements = document.select("#content .widthLimit a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, linkUrl, "");
                LOGGER.info("ProductPageUrl={}", linkUrl);
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        Future<DownloadResult> future = client.get("https://www.irunguns.us/product_categories.php", new DocumentCompletionHandler(webPage));
        return Observable.from(future, Schedulers.io()).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("irunguns.us") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("irunguns.us/frontPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
