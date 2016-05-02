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
    private static final Logger LOGGER = LoggerFactory.getLogger(WanstallsonlineFrontPageParser.class);
    private final HttpClient client;

    public WanstallsonlineFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, url, category);
        return webPageEntity;
    }

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
            String url;
            if (1 == i) {
                url = document.location();
            } else {
                url = document.location() + "index " + i + ".html";
            }
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, url, downloadResult.getSourcePage().getCategory());
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.wanstallsonline.com/firearms/", "firearm"));
        webPageEntities.add(create("http://www.wanstallsonline.com/optics/", "optic"));
        webPageEntities.add(create("http://www.wanstallsonline.com/tactical-accessories/", "misc"));
        webPageEntities.add(create("http://www.wanstallsonline.com/gun-cleaning/", "misc"));
        webPageEntities.add(create("http://www.wanstallsonline.com/storage-transport/", "misc"));
        webPageEntities.add(create("http://www.wanstallsonline.com/hunting-shooting-supplies/", "misc"));
        webPageEntities.add(create("http://www.wanstallsonline.com/firearms-ammunition", "ammo"));
        return Observable.from(webPageEntities)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.wanstallsonline.com/") && webPage.getType().equals("frontPage");
    }
}

