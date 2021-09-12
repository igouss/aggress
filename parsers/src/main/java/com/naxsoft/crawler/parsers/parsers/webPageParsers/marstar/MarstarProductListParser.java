package com.naxsoft.crawler.parsers.parsers.webPageParsers.marstar;

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
class MarstarProductListParser extends AbstractWebPageParser {
    private final HttpClient client;

    private static WebPageEntity getProductList(WebPageEntity parent, Element e, String category) {
        String linkUrl = e.attr("abs:href") + "&displayOutOfStock=no";
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "productList", linkUrl, category);
        log.info("Found product list page {} url={}", e.text(), linkUrl);
        return webPageEntity;
    }

    private static WebPageEntity getProductPage(WebPageEntity parent, Element e, String category) {
        String linkUrl = e.attr("abs:href");
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "productPage", linkUrl, category);
        log.info("Found product {} url={}", e.text(), linkUrl);
        return webPageEntity;
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            log.info("Parsing {}", document.select("h1").text());
            Elements elements = document.select("#main-content > div > table > tbody > tr > td > a:nth-child(3)");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductPage(downloadResult.getSourcePage(), e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
            elements = document.select(".baseTable td:nth-child(1) > a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductPage(downloadResult.getSourcePage(), e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
            elements = document.select("div.subcategoryName a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductList(downloadResult.getSourcePage(), e, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
//        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
//                .map(this::parseDocument)
//                .flatMap(Observable::from)
//                .toList().toBlocking().single();
        return null;
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "marstar.ca";
    }
}
