package com.naxsoft.parsers.webPageParsers.wanstallsonline;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class WanstallsonlineFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        int max = 1;
        Elements elements = document.select(".navigationtable td[valign=middle] > a");
        for (Element el : elements) {
            try {
                Matcher matcher = Pattern.compile("\\d+").matcher(el.text());
                if (matcher.find()) {
                    int num = Integer.parseInt(matcher.group());
                    if (num > max) {
                        max = num;
                    }
                }
            } catch (Exception ignored) {
                // ignore
            }
        }

        for (int i = 1; i < max; i++) {
            WebPageEntity webPageEntity = new WebPageEntity();
            if (1 == i) {
                webPageEntity.setUrl(document.location());
            } else {
                webPageEntity.setUrl(document.location() + "index " + i + ".html");
            }
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    public WanstallsonlineFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WanstallsonlineFrontPageParser.class);

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.wanstallsonline.com/firearms/"));
        return Observable.from(webPageEntities)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.wanstallsonline.com/") && webPage.getType().equals("frontPage");
    }
}

