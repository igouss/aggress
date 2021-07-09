package com.naxsoft.parsers.webPageParsers.westcoasthunting;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DocumentCompletionHandler;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class WestcoastHuntingProductListParser extends AbstractWebPageParser {
    private final HttpClient client;

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>();
        Document document = downloadResult.getDocument();
        if (document != null) {
            WebPageEntity sourcePage = downloadResult.getSourcePage();

            // Sub- categories
            Elements elements = document.select(".product-category > a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                log.info("Product sub-listing {}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }

            // Pagination
            if (!sourcePage.getUrl().contains("/page/")) {
                elements = document.select("a.page-numbers");
                int max = 0;
                for (Element e : elements) {
                    try {
                        max = Integer.parseInt(e.text());
                    } catch (Exception ignore) {
                    }
                }
                for (int i = 2; i < max; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(sourcePage, "", "productList", sourcePage.getUrl() + "page/" + i + "/", sourcePage.getCategory());
                    log.info("Product list subpage {} {}", i, webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }

            // Product pages
            elements = document.select(".product.instock a");
            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(sourcePage, "", "productPage", el.attr("abs:href"), sourcePage.getCategory());
                log.info("Product page {}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .map(this::parseDocument)
                .flatMap(Observable::from).toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "westcoasthunting.ca";
    }
}
