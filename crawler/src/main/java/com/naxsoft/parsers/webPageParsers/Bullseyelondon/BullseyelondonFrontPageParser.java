package com.naxsoft.parsers.webPageParsers.bullseyelondon;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.ning.http.client.ListenableFuture;
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

public class BullseyelondonFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BullseyelondonFrontPageParser.class);
    private final HttpClient client;

    public BullseyelondonFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);
        Elements elements = document.select(".vertnav-cat a");
        for (Element element : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(element.attr("abs:href") + "?limit=all");
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
            result.add(webPageEntity);
        }
        return result;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        ListenableFuture<DownloadResult> future = client.get(webPage.getUrl(), new DocumentCompletionHandler(webPage));
        return Observable.from(future).map(this::parseDocument).flatMap(Observable::from);
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("frontPage");
    }

}
