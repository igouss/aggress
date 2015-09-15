package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductListParser implements WebPageParser {

    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Logger logger = LoggerFactory.getLogger(this.getClass());

        Connection.Response e = client.get(webPage.getUrl());
        if (e.statusCode() == 200) {

            Document document = Jsoup.parse(e.body(), webPage.getUrl());
            if (isTerminalSubcategory(document)) {
                Elements subpages = document.select("#main footer > nav span, #main footer > nav a");
                if (subpages.size() != 0) {
                    for (Element subpage : subpages) {
                        try {
                            int page = Integer.parseInt(subpage.text());
                            e = client.get(webPage.getUrl() + "?pagenumber=" + page);
                            Document productListDocument = Jsoup.parse(e.body(), e.url().toString());
                            Elements products = productListDocument.select("#main > div > section > section:nth-child(1) > div > article > a");
                            for (Element product : products) {
                                WebPageEntity productPage = getWebPageEntity(webPage, logger, e, product);
                                result.add(productPage);
                            }
                        } catch (NumberFormatException e1) {
                            // ignore
                        }
                    }
                } else {
                    Elements products = document.select("#main > div > section > section:nth-child(1) > div > article > a");
                    for (Element product : products) {
                        WebPageEntity productPage = getWebPageEntity(webPage, logger, e, product);
                        result.add(productPage);
                    }
                }

            } else {
                Elements elements = document.select(".categories .links-list a");
                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(element.attr("abs:href"));
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(Integer.valueOf(e.statusCode()));
                    webPageEntity.setParent(webPage);
                    webPageEntity.setType("productList");
                    result.add(webPageEntity);
                }
            }
        }


        return result;
    }

    private boolean isTerminalSubcategory(Document document) {
        return document.select(".categories .active").size() != 0;
    }

    private WebPageEntity getWebPageEntity(WebPageEntity webPage, Logger logger, Connection.Response e, Element product) {
        WebPageEntity productPage = new WebPageEntity();
        productPage.setUrl(product.attr("abs:href"));
        productPage.setModificationDate(new Timestamp(System.currentTimeMillis()));
        productPage.setParsed(false);
        productPage.setStatusCode(Integer.valueOf(e.statusCode()));
        productPage.setType("productPage");
        productPage.setParent(webPage);
        logger.info("parseUrl=" + productPage.getUrl() + ", productListUrl=" + webPage.getUrl());
        return productPage;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productList");
    }
}

