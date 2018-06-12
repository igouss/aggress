package com.naxsoft.parsers.webPageParsers.westcoasthunting;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DocumentCompletionHandler;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WestcoastHuntingProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestcoastHuntingProductListParser.class);

    public WestcoastHuntingProductListParser(HttpClient client) {
        super(client);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>();
        Document document = downloadResult.getDocument();
        if (document != null) {
            WebPageEntity sourcePage = downloadResult.getSourcePage();

            // Sub- categories
            Elements elements = document.select(".product-category > a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product sub-listing {}", webPageEntity.getUrl());
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
                    LOGGER.info("Product list subpage {} {}", i, webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }

            // Product pages
            elements = document.select(".product.instock a");
            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(sourcePage, "", "productPage", el.attr("abs:href"), sourcePage.getCategory());
                LOGGER.info("Product page {}", webPageEntity.getUrl());
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
