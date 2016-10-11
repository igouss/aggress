package com.naxsoft.parsers.webPageParsers.Wolverinesupplies;

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

public class WolverinesuppliesFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesFrontPageParser.class);
    private final HttpClient client;

    private WolverinesuppliesFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".mainnav a");
            for (Element e : elements) {
                if (e.hasClass("sfSel") || e.text().equals("Range Events")) {
                    continue;
                }
                String linkUrl = e.attr("abs:href");
                if (null != linkUrl && !linkUrl.isEmpty() && linkUrl.contains("Products") && e.siblingElements().isEmpty()) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", linkUrl, e.text());
                    LOGGER.info("ProductPageUrl={}", linkUrl);
                    result.add(webPageEntity);
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
        return webPage.getUrl().contains("wolverinesupplies.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("wolverinesupplies.com/frontPage", (Message<WebPageEntity> event) -> {
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
