package com.naxsoft.parsers.webPageParsers.irunguns;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class IrungunsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(IrungunsFrontPageParser.class);

    private Collection<WebPageEntity> parseDocument(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("#content .widthLimit a");
        for (Element e : elements) {
            String linkUrl = e.attr("abs:href");
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(linkUrl);
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            LOGGER.info("ProductPageUrl={}", linkUrl);
            result.add(webPageEntity);
        }
        return result;
    }

    public IrungunsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> client.get("https://www.irunguns.us/product_categories.php", new DocumentCompletionHandler()));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("https://www.irunguns.us/") && webPage.getType().equals("frontPage");
    }
}
