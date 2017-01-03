package com.naxsoft.parsers.webPageParsers.firearmsoutletcanada;

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
class FirearmsoutletcanadaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirearmsoutletcanadaFrontPageParser.class);

    public FirearmsoutletcanadaFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".products-grid .product-name > a");

            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", el.attr("abs:href"), "n/a");
                LOGGER.trace("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result.build();
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        Set<WebPageEntity> webPageEntities = ImmutableSet.<WebPageEntity>builder()
                .add(create(parent, "http://www.firearmsoutletcanada.com/pistols.html?limit=all&stock_status=64", "firearm"))
                .add(create(parent, "http://www.firearmsoutletcanada.com/rifles.html?limit=all&stock_status=64", "firearm"))
                .add(create(parent, "http://www.firearmsoutletcanada.com/shotguns.html?limit=all&stock_status=64", "firearm"))
                .add(create(parent, "http://www.firearmsoutletcanada.com/ammo.html?limit=all&stock_status=64", "ammo"))
                .add(create(parent, "http://www.firearmsoutletcanada.com/accessories.html?limit=all&stock_status=64", "misc"))
                .add(create(parent, "http://www.firearmsoutletcanada.com/reloading.html?limit=all&stock_status=64", "reload"))
                .add(create(parent, "http://www.firearmsoutletcanada.com/parts.html?limit=all&stock_status=64", "misc"))
                .add(create(parent, "http://www.firearmsoutletcanada.com/sights-optics.html?limit=all&stock_status=64", "optic"))
                .add(create(parent, "http://www.firearmsoutletcanada.com/consignment.html?limit=all&stock_status=64", "firearm,optic"))
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
        return "firearmsoutletcanada.com";
    }

}
