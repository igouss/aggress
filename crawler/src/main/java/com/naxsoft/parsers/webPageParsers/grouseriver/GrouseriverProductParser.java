package com.naxsoft.parsers.webPageParsers.grouseriver;

import com.codahale.metrics.MetricRegistry;
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
import java.util.Objects;

public class GrouseriverProductParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrouseriverProductParser.class);

    public GrouseriverProductParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    @SuppressWarnings("unchecked")
    public Observable<WebPageEntity> parseJson(JsonResult downloadResult) {
        HashSet<WebPageEntity> result = new HashSet<>();
        Map parsedJson = downloadResult.getJson();
        List<Map<String, String>> items = (List<Map<String, String>>) parsedJson.get("items");
        for (Map<String, String> itemData : items) {
            LOGGER.info("Processing: " + itemData.get("displayname"));
            WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPageRaw", "http://www.grouseriver.com/" + itemData.get("urlcomponent"), downloadResult.getSourcePage().getCategory());
            result.add(webPageEntity);
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return client.get(parent.getUrl(), new JsonCompletionHandler(parent))
                .flatMap(this::parseJson)
                .flatMap(webPage -> PageDownloader.download(client, webPage, "productPageRaw")
                        .filter(Objects::nonNull))
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "grouseriver.com";
    }
}
