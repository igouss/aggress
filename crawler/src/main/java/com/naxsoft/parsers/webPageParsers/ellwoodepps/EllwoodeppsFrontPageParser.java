package com.naxsoft.parsers.webPageParsers.ellwoodepps;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class EllwoodeppsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsFrontPageParser.class);
    private static final Pattern productTotalPattern = Pattern.compile("of\\s(\\d+)");

    public EllwoodeppsFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, URL url, String category) {
        return new WebPageEntity(parent, "productList", url, category);
    }

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            String elements = document.select("#adj-nav-container > div.category-products > div.toolbar  div.amount-container > p").text();
            Matcher matcher = productTotalPattern.matcher(elements);
            if (!matcher.find()) {
                LOGGER.error("Unable to parse total pages");
                return Set.of();
            }

            int productTotal = Integer.parseInt(matcher.group(1));
            int pageTotal = (int) Math.ceil(productTotal / 100.0);

            for (int i = 1; i <= pageTotal; i++) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", document.location() + "&p=" + i,
                        downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "https://ellwoodepps.com/hunting/accessories.html?product_sold=3175&limit=100", "accessories"));
        webPageEntities.add(create(parent, "https://ellwoodepps.com/hunting/firearms.html?product_sold=3175&limit=100", "firearm"));

        return Observable.from(webPageEntities)
                .observeOn(Schedulers.io())
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseDocument)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "ellwoodepps.com";
    }

}