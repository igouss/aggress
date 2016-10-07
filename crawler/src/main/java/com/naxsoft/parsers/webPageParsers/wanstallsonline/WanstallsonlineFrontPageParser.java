package com.naxsoft.parsers.webPageParsers.wanstallsonline;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.eventbus.Message;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class WanstallsonlineFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WanstallsonlineFrontPageParser.class);
    private static final Pattern pageNumPattern = Pattern.compile("\\d+");
    private final HttpClient client;

    private WanstallsonlineFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", false, url, category);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();

        if (document != null) {
            int max = 1;
            Elements elements = document.select(".navigationtable td[valign=middle] > a");
            for (Element el : elements) {
                try {
                    Matcher matcher = pageNumPattern.matcher(el.text());
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
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", false, url, downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/firearms/", "firearm"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/optics/", "optic"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/tactical-accessories/", "misc"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/gun-cleaning/", "misc"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/storage-transport/", "misc"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/hunting-shooting-supplies/", "misc"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/firearms-ammunition", "ammo"));
        return Observable.from(webPageEntities)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("wanstallsonline.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("wanstallsonline.com/frontPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}

