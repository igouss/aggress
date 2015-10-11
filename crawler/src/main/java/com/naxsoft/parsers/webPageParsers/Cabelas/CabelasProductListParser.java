package com.naxsoft.parsers.webPageParsers.cabelas;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductListParser implements WebPageParser {
    private final AsyncFetchClient client;
    private final Logger logger;

    public CabelasProductListParser(AsyncFetchClient client) {
        this.client = client;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {

        Future<Set<Document>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<Document>>() {
            @Override
            public Set<Document> onCompleted(Response resp) throws Exception {
                HashSet<Document> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    webPage.setParsed(true);
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    result.add(document);
                }
                return result;
            }
        });
        // return Observable.defer(() -> Observable.just(future.get()));
        return Observable.from(future).map(documents -> {
            HashSet<WebPageEntity> result = new HashSet<>();
            for (Document document : documents) {
                if (isTerminalSubcategory(document)) {
                    if (document.baseUri().contains("pagenumber")) {
                        Elements elements = document.select(".productCard-heading a");
                        for (Element element : elements) {
                            result.add(productPage(webPage.getParent(), 200, element.attr("abs:href")));
                        }
                    } else {
                        Elements subPages = document.select("#main footer > nav span, #main footer > nav a");
                        if (subPages.size() != 0) {
                            int max = 1;
                            for (Element subpage : subPages) {
                                try {
                                    int page = Integer.parseInt(subpage.text());
                                    if (page > max) {
                                        max = page;
                                    }
                                } catch (Exception e) {
                                    // ignore
                                }
                            }
                            for (int i = 1; i <= max; i++) {
                                result.add(getProductList(webPage, 200, webPage.getUrl() + "?pagenumber=" + i));
                            }
                        } else {
                            result.add(getProductList(webPage, 200, webPage.getUrl() + "?pagenumber=" + 1));
                        }
                    }
                } else {
                    Elements subPages = document.select("#categories > ul > li > a");
                    for (Element element : subPages) {
                        WebPageEntity subCategoryPage = getProductList(webPage, 200, element.attr("abs:href"));
                        //subCategoryPage.setContent(document.html());
                        result.add(subCategoryPage);
                    }
                }
            }
            return result;
        });
//        .map(webPageEntities -> {
//            HashSet<WebPageEntity> result = new HashSet<>();
//            for (WebPageEntity webPageEntity : webPageEntities) {
//                if (webPageEntity.getType().equals("productPage")) {
//                    result.add(webPageEntity);
//                } else {
//                    try {
//                        parse(webPageEntity).map(result::addAll).subscribe();
//                    } catch (Exception e) {
//                        logger.error("Failed to parse", e);
//                    }
//                }
//            }
//            return result;
//        });


//            Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
//                @Override
//                public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
//                    HashSet<WebPageEntity> result = new HashSet<>();
//                    if (resp.getStatusCode() == 200) {
//                        Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
//                        Elements subpages = document.select("#main > div > section > section:nth-child(1) h6 a");
//                        for (Element subpage : subpages) {
//                            result.add(productPage(webPage.getParent(), resp.getStatusCode(), subpage.attr("abs:href")));
//                        }
//                    }
//                    return result;
//                }
//            });
//        // return Observable.defer(() -> Observable.just(future.get()));
//        return Observable.defer(() -> Observable.from(future));
    }

//    private Observable<Set<WebPageEntity>> getProductListPages(final WebPageEntity root, final WebPageEntity parent) {
//        return Observable.defer(() -> Observable.from(client.get(parent.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
//            @Override
//            public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
//                HashSet<WebPageEntity> result = new HashSet<>();
//                if (resp.getStatusCode() == 200) {
//                    Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
//                    if (!isTerminalSubcategory(document)) {
//                        Elements subPages = document.select("#categories > ul > li > a");
//                        for (Element element : subPages) {
//                            WebPageEntity productPage = getProductList(root, resp.getStatusCode(), element.attr("abs:href"));
//                            productPage.setContent(resp.getResponseBody());
//                            result.add(productPage);
//                        }
//                    } else {
//                        parent.setContent(resp.getResponseBody());
//                        parent.setParent(root);
//                        result.add(parent);
//                    }
//                }
//                return result;
//            }
//        }))).flatMap(set -> Observable.from(set).map(webPageEntity -> {
//            HashSet<WebPageEntity> result = new HashSet<>();
//            if (isTerminalSubcategory(Jsoup.parse(webPageEntity.getContent()))) {
//                result.add(webPageEntity);
//            } else {
//                getProductListPages(root, webPageEntity).subscribe(pageSet ->
//                                result.addAll(pageSet)
//                );
//            }
//            return result;
//        })).flatMap(set -> Observable.from(set).map(webPageEntity -> {
//            Document document = Jsoup.parse(webPageEntity.getContent());
//            Elements subpages = document.select("#main footer > nav span, #main footer > nav a");
//            HashSet<WebPageEntity> result = new HashSet<>();
//            if (subpages.size() != 0) {
//                int max = 1;
//                for (Element subpage : subpages) {
//                    try {
//                        int page = Integer.parseInt(subpage.text());
//                        if (page > max) {
//                            max = page;
//                        }
//                    } catch (Exception e) {
//                        // ignore
//                    }
//                }
//                for (int i= 1; i <= max; i++) {
//                    result.add(getProductList(webPageEntity, 200, webPageEntity.getUrl() + "?pagenumber=" + i));
//                }
//            } else {
//                result.add(getProductList(webPageEntity, 200, webPageEntity.getUrl()));
//            }
//            return result;
//        }));
//    }

    private WebPageEntity getProductList(WebPageEntity parent, int statusCode, String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(statusCode);
        webPageEntity.setParent(parent);
        webPageEntity.setType("productList");
        logger.info("productList=" + webPageEntity.getUrl() + ", parent=" + parent.getUrl());
        return webPageEntity;
    }


    private boolean isTerminalSubcategory(Document document) {
        return (document.select(".categories .active").size() == 1) || document.select("h1").text().equals("Thanks for visiting Cabelas.ca!");
    }

    private WebPageEntity productPage(WebPageEntity parent, int statusCode, String url) {
        WebPageEntity productPage = new WebPageEntity();
        productPage.setUrl(url);
        productPage.setModificationDate(new Timestamp(System.currentTimeMillis()));
        productPage.setParsed(false);
        productPage.setStatusCode(statusCode);
        productPage.setType("productPage");
        productPage.setParent(parent);
        return productPage;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productList");
    }
}

