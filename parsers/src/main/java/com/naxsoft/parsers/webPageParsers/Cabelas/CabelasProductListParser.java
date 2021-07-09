package com.naxsoft.parsers.webPageParsers.Cabelas;

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
class CabelasProductListParser extends AbstractWebPageParser {
    private final HttpClient client;

    private static boolean isTerminalSubcategory(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();
        boolean isTerminalCategory = (1 == document.select(".categories .active").size()) || document.select("h1").text().equals("Thanks for visiting Cabelas.ca!");
        if (isTerminalCategory) {
            log.info("Terminal category {}", downloadResult.getSourcePage().getUrl());
        } else {
            log.info("Non-terminal category {}", downloadResult.getSourcePage().getUrl());
        }
        return isTerminalCategory;
    }

    private static WebPageEntity getProductList(WebPageEntity parent, String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "productList", url, category);
        log.info("productList={}", webPageEntity.getUrl());
        return webPageEntity;
    }

    private static WebPageEntity productPage(WebPageEntity parent, String url, String category) {
        WebPageEntity productPage = new WebPageEntity(parent, "", "productPage", url, category);
        log.info("productPage={}", url);
        return productPage;
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
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
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .toList().toBlocking().single();
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

