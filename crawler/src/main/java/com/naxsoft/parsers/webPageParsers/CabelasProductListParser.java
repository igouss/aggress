package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductListParser implements WebPageParser {

    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        AsyncFetchClient<Set<WebPageEntity>> client = new AsyncFetchClient<>();

        Logger logger = LoggerFactory.getLogger(this.getClass());
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    if (isTerminalSubcategory(document)) {
                        Elements subpages = document.select("#main footer > nav span, #main footer > nav a");
                        if (subpages.size() != 0) {
                            for (Element subpage : subpages) {
                                try {
                                    int page = Integer.parseInt(subpage.text());
                                    AsyncFetchClient<Set<WebPageEntity>> client2 = new AsyncFetchClient<>();
                                    Future<Set<WebPageEntity>> future2 = client2.get(webPage.getUrl() + "?pagenumber=" + page, new AsyncCompletionHandler<Set<WebPageEntity>>() {
                                        @Override
                                        public Set<WebPageEntity> onCompleted(Response response) throws Exception {
                                            Document productListDocument = Jsoup.parse(response.getResponseBody(), response.getUri().toString());
                                            Elements products = productListDocument.select("#main > div > section > section:nth-child(1) > div > article > a");
                                            HashSet<WebPageEntity> res = new HashSet<>();
                                            for (Element product : products) {
                                                WebPageEntity productPage = productPage(webPage, logger, response.getStatusCode(), product);
                                                res.add(productPage);
                                            }
                                            return res;
                                        }
                                    });
                                    result.addAll(future2.get());
                                    client2.close();
                                } catch (NumberFormatException e) {
                                    // ignore
                                }
                            }
                        } else {
                            Elements products = document.select("#main > div > section > section:nth-child(1) > div > article > a");
                            for (Element product : products) {
                                WebPageEntity productPage = productPage(webPage, logger, resp.getStatusCode(), product);
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
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setParent(webPage);
                            webPageEntity.setType("productList");
                            logger.info("productList=" + webPageEntity.getUrl() + ", parent=" + webPage.getUrl());
                            result.add(webPageEntity);
                        }
                    }
                }
                return result;
            }
        });
        try {
            Set<WebPageEntity> webPageEntities = future.get();
            client.close();
            return webPageEntities;
        } catch (Exception e) {
            logger.error("HTTP error", e);
            return new HashSet<>();
        }
    }

    private boolean isTerminalSubcategory(Document document) {
        return document.select(".categories .active").size() == 1;
    }

    private WebPageEntity productPage(WebPageEntity parent, Logger logger, int statusCode, Element product) {
        WebPageEntity productPage = new WebPageEntity();
        productPage.setUrl(product.attr("abs:href"));
        productPage.setModificationDate(new Timestamp(System.currentTimeMillis()));
        productPage.setParsed(false);
        productPage.setStatusCode(statusCode);
        productPage.setType("productPage");
        productPage.setParent(parent);
        logger.info("productPage = " + productPage.getUrl() + ", productList = " + parent.getUrl());
        return productPage;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productList");
    }
}

