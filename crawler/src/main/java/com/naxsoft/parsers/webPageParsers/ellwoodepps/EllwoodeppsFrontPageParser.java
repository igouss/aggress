package com.naxsoft.parsers.webPageParsers.ellwoodepps;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
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
class EllwoodeppsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsFrontPageParser.class);
    private static final Pattern productTotalPattern = Pattern.compile("of\\s(\\d+)");
    private final HttpClient client;

    private EllwoodeppsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            String elements = document.select("#adj-nav-container > div.category-products > div.toolbar  div.amount-container > p").text();
            Matcher matcher = productTotalPattern.matcher(elements);
            if (!matcher.find()) {
                LOGGER.error("Unable to parse total pages");
                return Observable.empty();
            }

            int productTotal = Integer.parseInt(matcher.group(1));
            int pageTotal = (int) Math.ceil(productTotal / 100.0);

            for (int i = 1; i <= pageTotal; i++) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "&p=" + i, downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "https://ellwoodepps.com/hunting/accessories.html?product_sold=3175&limit=100", "accessories"));
        webPageEntities.add(create(parent, "https://ellwoodepps.com/hunting/firearms.html?product_sold=3175&limit=100", "firearm"));

        return Observable.from(webPageEntities)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("ellwoodepps.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("ellwoodepps.com/frontPage", getParseRequestMessageHandler());
    }
}