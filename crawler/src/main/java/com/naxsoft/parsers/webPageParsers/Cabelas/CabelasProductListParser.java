package com.naxsoft.parsers.webPageParsers.cabelas;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class CabelasProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CabelasProductListParser.class);

    public CabelasProductListParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static boolean isTerminalSubcategory(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();
        boolean isTerminalCategory = (1 == document.select(".categories .active").size()) || document.select("h1").text().equals("Thanks for visiting Cabelas.ca!");
        if (isTerminalCategory) {
            LOGGER.info("Terminal category {}", downloadResult.getSourcePage().getUrl());
        } else {
            LOGGER.info("Non-terminal category {}", downloadResult.getSourcePage().getUrl());
        }
        return isTerminalCategory;
    }

    private static WebPageEntity getProductList(WebPageEntity parent, String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "productList", url, category);
        LOGGER.info("productList={}", webPageEntity.getUrl());
        return webPageEntity;
    }

    private static WebPageEntity productPage(WebPageEntity parent, String url, String category) {
        WebPageEntity productPage = new WebPageEntity(parent, "", "productPage", url, category);
        LOGGER.info("productPage={}", url);
        return productPage;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            if (isTerminalSubcategory(downloadResult)) {
                if (document.baseUri().contains("pagenumber")) {
                    Elements elements = document.select("section > section .productCard-heading a");
                    for (Element element : elements) {
                        WebPageEntity webPageEntity = productPage(downloadResult.getSourcePage(), element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                        result.add(webPageEntity);
                    }
                } else {
                    Elements subPages = document.select("#main footer > nav span, #main footer > nav a");
                    if (!subPages.isEmpty()) {
                        int max = 1;
                        for (Element subpage : subPages) {
                            try {
                                int page = Integer.parseInt(subpage.text());
                                if (page > max) {
                                    max = page;
                                }
                            } catch (Exception ignored) {
                                // ignore
                            }
                        }
                        for (int i = 1; i <= max; i++) {
                            WebPageEntity webPageEntity = getProductList(downloadResult.getSourcePage(), document.location() + "?pagenumber=" + i, downloadResult.getSourcePage().getCategory());
                            result.add(webPageEntity);
                        }
                    } else {
                        WebPageEntity webPageEntity = getProductList(downloadResult.getSourcePage(), document.location() + "?pagenumber=" + 1, downloadResult.getSourcePage().getCategory());
                        result.add(webPageEntity);
                    }
                }
            } else {
                Elements subPages = document.select("#categories > ul > li > a");
                for (Element element : subPages) {
                    String category;
                    if (downloadResult.getSourcePage().getCategory() == null || downloadResult.getSourcePage().getCategory().isEmpty()) {
                        category = element.text();
                    } else {
                        category = downloadResult.getSourcePage().getCategory();
                    }
                    WebPageEntity subCategoryPage = getProductList(downloadResult.getSourcePage(), element.attr("abs:href"), category);
                    result.add(subCategoryPage);
                }
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity))
                .flatMap(this::parseDocument)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "cabelas.ca";
    }

}

