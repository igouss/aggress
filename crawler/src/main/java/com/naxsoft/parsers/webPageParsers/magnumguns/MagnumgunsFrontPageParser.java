package com.naxsoft.parsers.webPageParsers.magnumguns;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class MagnumgunsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(MagnumgunsFrontPageParser.class);

    private Collection<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();
        Set<WebPageEntity> result = new HashSet<>(1);
        Elements elements = document.select(".product-category > a");
        for (Element e : elements) {
            String url = e.attr("abs:href");
            result.add(create(url, e.text()));
        }
        return result;
    }

    private Collection<WebPageEntity> parseSubPages(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Document document = downloadResult.getDocument();
        Elements elements = document.select(".woocommerce-pagination li > .page-numbers");
        int max = 0;
        for (Element element : elements) {
            try {
                int tmp = Integer.parseInt(element.text());
                if (tmp > max) {
                    max = tmp;
                }
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }

        for (int i = 1; i <= max; i++) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(downloadResult.getSourcePage().getUrl() + "/page/" + i + "/");
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory(downloadResult.getSourcePage().getCategory());
            LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
            result.add(webPageEntity);
        }

        return result;
    }

    public MagnumgunsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.from(client.get("http://www.magnumguns.ca/shop/", new DocumentCompletionHandler(parent)))
                .map(this::parseFrontPage)
                .flatMap(Observable::from)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseSubPages)
                .flatMap(Observable::from);
    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        webPageEntity.setCategory(category);
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.magnumguns.ca/") && webPage.getType().equals("frontPage");
    }
}