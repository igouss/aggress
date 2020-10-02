package com.naxsoft.parsers.webPageParsers.firearmsoutletcanada;

import java.util.HashSet;
import java.util.Set;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class FirearmsoutletcanadaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirearmsoutletcanadaFrontPageParser.class);

    private static WebPageEntity create(WebPageEntity parent, URL url, String category) {
        return new WebPageEntity(parent, "productList", url, category);
    }

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".products-grid .product-name > a");

            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productPage", el.attr("abs:href"), "n/a");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/pistols.html?limit=all&stock_status=64", "firearm"));
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/rifles.html?limit=all&stock_status=64", "firearm"));
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/shotguns.html?limit=all&stock_status=64", "firearm"));
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/ammo.html?limit=all&stock_status=64", "ammo"));
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/accessories.html?limit=all&stock_status=64", "misc"));
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/reloading.html?limit=all&stock_status=64", "reload"));
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/parts.html?limit=all&stock_status=64", "misc"));
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/sights-optics.html?limit=all&stock_status=64", "optic"));
        webPageEntities.add(create(parent, "http://www.firearmsoutletcanada.com/consignment.html?limit=all&stock_status=64", "firearm,optic"));

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
        return "firearmsoutletcanada.com";
    }

}
