package com.naxsoft.parsers.webPageParsers.westcoasthunting;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

class WestcoastHuntingFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestcoastHuntingFrontPageParser.class);

    public WestcoastHuntingFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();
        try {
            Document document = downloadResult.getDocument();
            Elements elements = document.select(".product-category > a");
            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", el.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse", e);

        }
        return result.build();
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        Set<WebPageEntity> webPageEntities = ImmutableSet.<WebPageEntity>builder()
                .add(create(parent, "http://www.westcoasthunting.ca/product-category/firearms/", "firearm"))
                .add(create(parent, "http://www.westcoasthunting.ca/product-category/optics/", "optic"))
                .add(create(parent, "http://www.westcoasthunting.ca/product-category/optic-accessories/", "optic"))
                .add(create(parent, "http://www.westcoasthunting.ca/product-category/firearms-accessories/", "misc"))
                .add(create(parent, "http://www.westcoasthunting.ca/product-category/gun-maintenance/", "misc"))
                .add(create(parent, "http://www.westcoasthunting.ca/product-category/ammunition/", "ammo"))
                .build();

        return Flowable.fromIterable(webPageEntities)
                .observeOn(Schedulers.io())
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMapIterable(this::parseDocument)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "westcoasthunting.ca";
    }
}

