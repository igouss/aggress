package com.naxsoft.parsers.webPageParsers.gunhub;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class GunhubFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".product-name a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                log.info("productPage={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.gunhub.ca/apps/webstore/products/category/1479959", "firearm"));
        webPageEntities.add(create(parent, "http://www.gunhub.ca/apps/webstore/products/category/1479930", "firearm"));
        webPageEntities.add(create(parent, "http://www.gunhub.ca/apps/webstore/products/category/1479960", "firearm"));
        webPageEntities.add(create(parent, "http://www.gunhub.ca/apps/webstore/products/category/1487024", "ammo"));

//        return Observable.from(webPageEntities)
//                .map(page -> client.get(page.getUrl(), new DocumentCompletionHandler(page)))
//                .flatMap(Observable::from)
//                .map(this::parseDocument)
//                .flatMap(Observable::from)
//                .toList().toBlocking().single();
        return null;
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "gunhub.ca";
    }
}