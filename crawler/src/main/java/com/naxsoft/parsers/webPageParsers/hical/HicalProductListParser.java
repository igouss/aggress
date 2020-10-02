package com.naxsoft.parsers.webPageParsers.hical;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class HicalProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalProductListParser.class);

    private Iterable<WebPageEntity> parseDocument(WebPageEntity webPageEntity) throws Exception {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = Jsoup.parse(webPageEntity.getUrl(), 1000);
        if (document != null) {
            Elements elements;
            // Add sub categories
            if (!document.location().contains("page=")) {
                // add subpages
                elements = document.select("#CategoryPagingTop > div > ul > li > a");
                for (Element el : elements) {
                    LOGGER.info("ProductList sub-page {}", webPageEntity.getUrl());
                    result.add(new WebPageEntity(webPageEntity, "productList", new URL(el.attr("abs:href")),
                            webPageEntity.getCategory()));
                }
            }

            elements = document.select("#frmCompare .ProductDetails a");
            for (Element el : elements) {
                result.add(new WebPageEntity(webPageEntity, "productPage", new URL(el.attr("abs:href")),
                        webPageEntity.getCategory()));
            }
        }
        return result;
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity webPageEntity) throws Exception {
        return parseDocument(webPageEntity);
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "hical.ca";
    }

}