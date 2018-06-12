package com.naxsoft.parsers.webPageParsers.grouseriver;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import com.naxsoft.http.JsonCompletionHandler;
import com.naxsoft.http.JsonResult;
import com.naxsoft.http.PageDownloader;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;


public class GrouseriverProductParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrouseriverProductParser.class);

    public GrouseriverProductParser(HttpClient client) {
        super(client);
    }

    @SuppressWarnings("unchecked")
    public Set<WebPageEntity> parseJson(JsonResult downloadResult) {
        HashSet<WebPageEntity> result = new HashSet<>();
        Map<String, List<Map<String, String>>> parsedJson = downloadResult.getJson();
        List<Map<String, String>> items = parsedJson.get("items");
        for (Map<String, String> itemData : items) {
            LOGGER.info("Processing: " + itemData.get("displayname"));
            WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPageRaw", "http://www.grouseriver.com/" + itemData.get("urlcomponent"), downloadResult.getSourcePage().getCategory());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return Observable.from(client.get(webPageEntity.getUrl(), new JsonCompletionHandler(webPageEntity)))
                .map(this::parseJson)
                .flatMap(Observable::from)
                .map(webPage -> PageDownloader.download(client, webPage, "productPageRaw"))
                .filter(Objects::nonNull)
                .flatMap(Observable::from)
                .toList().toBlocking().single();
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
