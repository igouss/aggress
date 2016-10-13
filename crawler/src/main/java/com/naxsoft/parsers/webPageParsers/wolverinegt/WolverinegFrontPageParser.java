package com.naxsoft.parsers.webPageParsers.wolverinegt;

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

import java.util.HashSet;
import java.util.Set;

public class WolverinegFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinegFrontPageParser.class);
    private final HttpClient client;

    private WolverinegFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".product-wrapper h3 a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");
                if (null != linkUrl && !linkUrl.isEmpty()) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", linkUrl, downloadResult.getSourcePage().getCategory());
                    LOGGER.info("ProductPageUrl={}", linkUrl);
                    result.add(webPageEntity);
                }
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://wolverinegt.ca/product-category/firearms/hand-guns/?products-per-page=all", "firearm"));
        webPageEntities.add(create(parent, "http://wolverinegt.ca/product-category/firearms/shot-guns/?products-per-page=all", "firearm"));
        webPageEntities.add(create(parent, "http://wolverinegt.ca/product-category/firearms/rifles/?products-per-page=all", "firearm"));

        return Observable.from(webPageEntities)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("wolverinegt.ca") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("wolverinegt.ca/frontPage", getParseRequestMessageHandler());
    }
}
