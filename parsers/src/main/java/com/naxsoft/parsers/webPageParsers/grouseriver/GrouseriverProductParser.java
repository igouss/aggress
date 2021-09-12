package com.naxsoft.parsers.webPageParsers.grouseriver;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import com.naxsoft.http.JsonResult;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class GrouseriverProductParser extends AbstractWebPageParser {
    private final HttpClient client;

    public Set<WebPageEntity> parseJson(JsonResult downloadResult) {
        HashSet<WebPageEntity> result = new HashSet<>();
        Map<String, List<Map<String, String>>> parsedJson = downloadResult.getJson();
        List<Map<String, String>> items = parsedJson.get("items");
        for (Map<String, String> itemData : items) {
            log.info("Processing: " + itemData.get("displayname"));
            WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPageRaw", "http://www.grouseriver.com/" + itemData.get("urlcomponent"), downloadResult.getSourcePage().getCategory());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
//        return Observable.from(client.get(webPageEntity.getUrl(), new JsonCompletionHandler(webPageEntity)))
//                .map(this::parseJson)
//                .flatMap(Observable::from)
//                .map(webPage -> PageDownloader.download(client, webPage, "productPageRaw"))
//                .filter(Objects::nonNull)
//                .flatMap(Observable::from)
//                .toList().toBlocking().single();
        return null;
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
