package com.naxsoft.parsers.webPageParsers.Wolverinesupplies;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class WolverinesuppliesProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesProductPageParser.class);
    private final HttpClient client;

    public WolverinesuppliesProductPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return PageDownloader.download(client, webPage, "productPageRaw").filter(data -> null != data);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("wolverinesupplies.com") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("wolverinesupplies.com/productPage", (Message<WebPageEntity> event) -> {
            parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err));
        });
    }
}
