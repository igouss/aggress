package com.naxsoft.parsers.webPageParsers.grouseriver;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.JsonCompletionHandler;
import com.naxsoft.parsers.webPageParsers.JsonResult;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GrouseriverProductParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrouseriverProductParser.class);
    private final HttpClient client;

    private GrouseriverProductParser(HttpClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parseJson(JsonResult downloadResult) {
        HashSet<WebPageEntity> result = new HashSet<>();
        Map parsedJson = downloadResult.getJson();
        List<Map<String, String>> items = (List<Map<String, String>>) parsedJson.get("items");
        for (Map<String, String> itemData : items) {
            LOGGER.info("Processing: " + itemData.get("displayname"));
            result.add(new WebPageEntity(downloadResult.getSourcePage(), "", "productPageRaw", "http://www.grouseriver.com/" + itemData.get("urlcomponent"), downloadResult.getSourcePage().getCategory()));
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return client.get(parent.getUrl(), new JsonCompletionHandler(parent))
                .flatMap(this::parseJson)
                .flatMap(webPage -> PageDownloader.download(client, webPage, "productPageRaw")
                        .filter(data -> null != data));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("grouseriver.com") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("grouseriver.com/productPage", getParseRequestMessageHandler());
    }
}
