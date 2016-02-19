package com.naxsoft.parsers.webPageParsers.prophetriver;

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
public class ProphetriverFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProphetriverFrontPageParser.class);

    private Collection<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();
        Set<WebPageEntity> result = new HashSet<>(1);
        Elements elements = document.select("#AccordianWrapper a");
        for (Element e : elements) {
            result.add(create(e.attr("abs:href"), null));
        }
        return result;
    }

    public ProphetriverFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.from(client.get("http://store.prophetriver.com/categories/", new DocumentCompletionHandler(parent)))
                .map(this::parseFrontPage)
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
        return webPage.getUrl().startsWith("http://store.prophetriver.com/") && webPage.getType().equals("frontPage");
    }
}