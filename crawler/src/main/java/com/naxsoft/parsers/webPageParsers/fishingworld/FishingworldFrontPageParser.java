package com.naxsoft.parsers.webPageParsers.fishingworld;

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
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class FishingworldFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FishingworldFrontPageParser.class);
    private static final Pattern maxPagesPattern = Pattern.compile("(\\d+) of (\\d+)");

    public FishingworldFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, URL url, String category) {
        return new WebPageEntity(parent, "productList", url, category);
    }

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#list > div.bar.blue");
            Matcher matcher = maxPagesPattern.matcher(elements.text());
            if (matcher.find()) {
                int max = Integer.parseInt(matcher.group(2));
                int postsPerPage = 10;
                int pages = (int) Math.ceil((double) max / postsPerPage);

                for (int i = 1; i <= pages; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", document.location() + "?page=" + i,
                            downloadResult.getSourcePage().getCategory());
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            } else {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", document.location(),
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
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/66-guns", "firearm"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/67-ammunition", "ammo"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/66-guns", "firearm"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/146-optics", "optic"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/144-shooting-accesories", "misc"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/185-tree-stands", "misc"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/65-accessories", "misc"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/205-pellet-gun", "firearm"));

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
        return "fishingworld.ca";
    }

}