package com.naxsoft.parsers.webPageParsers.gotenda;

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
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class GotendaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GotendaFrontPageParser.class);
    private final HttpClient client;

    public GotendaFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, url, category);
        return webPageEntity;
    }

    private Collection<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();
        Set<WebPageEntity> result = new HashSet<>(1);
        Elements elements = document.select(".HorizontalDisplay> li.NavigationElement > a");
        for (Element e : elements) {
            String url = e.attr("abs:href") + "&PageSize=60&Page=1";
            result.add(create(url, e.text()));
        }
        return result;
    }

    private Collection<WebPageEntity> parseSubPages(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();
        Set<WebPageEntity> result = new HashSet<>(1);
        Elements elements = document.select(".InfoArea h3 a");
        for (Element e : elements) {
            result.add(create(e.attr("abs:href") + "&PageSize=60&Page=1", downloadResult.getSourcePage().getCategory()));
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.from(client.get(parent.getUrl(), new DocumentCompletionHandler(parent)), Schedulers.io())
                .map(this::parseFrontPage)
                .flatMap(Observable::from)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseSubPages)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://gotenda.com/") && webPage.getType().equals("frontPage");
    }
}