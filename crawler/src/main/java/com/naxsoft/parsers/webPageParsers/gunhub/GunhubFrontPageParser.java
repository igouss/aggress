package com.naxsoft.parsers.webPageParsers.gunhub;

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

class GunhubFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GunhubFrontPageParser.class);

    public GunhubFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".product-name a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("productPage={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result.build();
    }


    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        Set<WebPageEntity> webPageEntities = ImmutableSet.<WebPageEntity>builder()
                .add(create(parent, "http://www.gunhub.ca/apps/webstore/products/category/1479959", "firearm"))
                .add(create(parent, "http://www.gunhub.ca/apps/webstore/products/category/1479930", "firearm"))
                .add(create(parent, "http://www.gunhub.ca/apps/webstore/products/category/1479960", "firearm"))
                .add(create(parent, "http://www.gunhub.ca/apps/webstore/products/category/1487024", "ammo"))
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
        return "gunhub.ca";
    }


}