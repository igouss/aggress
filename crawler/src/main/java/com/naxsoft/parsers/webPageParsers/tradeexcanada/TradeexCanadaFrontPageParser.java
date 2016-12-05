package com.naxsoft.parsers.webPageParsers.tradeexcanada;

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

/**
 * Copyright NAXSoft 2015
 */
class TradeexCanadaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaFrontPageParser.class);

    public TradeexCanadaFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url) {
        return new WebPageEntity(parent, "", "productList", url, "N/A");
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".view-content a");
            for (Element element : elements) {
                String category;
                if (element.text().contains("AAA Super Specials")) {
                    category = "firearm,ammo";
                } else if (element.text().contains("Combination Guns")) {
                    category = "firearm";
                } else if (element.text().contains("Double Rifles")) {
                    category = "firearm";
                } else if (element.text().contains("Handguns")) {
                    category = "firearm";
                } else if (element.text().contains("Hunting and Sporting Arms")) {
                    category = "firearm";
                } else if (element.text().contains("Rifle")) {
                    category = "firearm";
                } else if (element.text().contains("Shotguns")) {
                    category = "firearm";
                } else if (element.text().contains("Ammunition")) {
                    category = "ammo";
                } else if (element.text().contains("Reloading Components")) {
                    category = "reload";
                } else if (element.text().contains("Scopes")) {
                    category = "optic";
                } else {
                    category = "misc";
                }

                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", element.attr("abs:href"), category);
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result.build();
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        Set<WebPageEntity> webPageEntities = ImmutableSet.<WebPageEntity>builder()
                .add(create(parent, "https://www.tradeexcanada.com/products_list"))
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
        return "tradeexcanada.com";
    }

}