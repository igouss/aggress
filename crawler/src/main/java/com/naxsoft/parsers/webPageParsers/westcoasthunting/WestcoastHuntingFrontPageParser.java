package com.naxsoft.parsers.webPageParsers.westcoasthunting;

import com.codahale.metrics.MetricRegistry;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;

public class WestcoastHuntingFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestcoastHuntingFrontPageParser.class);

    public WestcoastHuntingFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return WebPageEntity.legacyCreate(parent, "", "productList", url, category);
    }

    private Flux<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        return Flux.create(emitter -> {
            try {
                Document document = downloadResult.getDocument();
                Elements elements = document.select(".product-category > a");
                for (Element el : elements) {
                    WebPageEntity webPageEntity = WebPageEntity.legacyCreate(downloadResult.getSourcePage(), "", "productList", el.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    emitter.next(webPageEntity);
                }
                emitter.complete();
            } catch (Exception e) {
                LOGGER.error("Failed to parse", e);
                emitter.complete();
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    @Override
    public Flux<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/firearms/", "firearm"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/optics/", "optic"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/optic-accessories/", "optic"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/firearms-accessories/", "misc"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/gun-maintenance/", "misc"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/ammunition/", "ammo"));
        return Flux.fromIterable(webPageEntities)
                .publishOn(Schedulers.boundedElastic())
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
        return "westcoasthunting.ca";
    }
}

