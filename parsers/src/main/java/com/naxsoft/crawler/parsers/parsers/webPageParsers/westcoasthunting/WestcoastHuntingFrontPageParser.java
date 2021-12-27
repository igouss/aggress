package com.naxsoft.crawler.parsers.parsers.webPageParsers.westcoasthunting;

import com.naxsoft.common.entity.WebPageEntity;
import com.naxsoft.crawler.parsers.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
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
public class WestcoastHuntingFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>();
        Document document = downloadResult.getDocument();
        Elements elements = document.select(".product-category > a");
        for (Element el : elements) {
            WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", el.attr("abs:href"), downloadResult.getSourcePage().getCategory());
            log.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/firearms/", "firearm"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/optics/", "optic"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/optic-accessories/", "optic"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/firearms-accessories/", "misc"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/gun-maintenance/", "misc"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/ammunition/", "ammo"));
//        return Observable.from(webPageEntities)
//                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
//                .flatMap(Observable::from)
//                .map(this::parseDocument)
//                .flatMap(Observable::from).toList().toBlocking().single();
        return null;
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "westcoasthunting.ca";
    }
}
