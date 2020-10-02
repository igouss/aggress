package com.naxsoft.parsers.webPageParsers.westcoasthunting;

import java.util.HashSet;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WestcoastHuntingFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestcoastHuntingFrontPageParser.class);

    public WestcoastHuntingFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, URL url, String category) {
        return new WebPageEntity(parent, "productList", url, category);
    }

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        return Observable.create(emitter -> {
            try {
                Document document = downloadResult.getDocument();
                Elements elements = document.select(".product-category > a");
                for (Element el : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", el.attr("abs:href"),
                            downloadResult.getSourcePage().getCategory());
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    emitter.onNext(webPageEntity);
                }
                emitter.onCompleted();
            } catch (Exception e) {
                LOGGER.error("Failed to parse", e);
                emitter.onCompleted();
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/firearms/", "firearm"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/optics/", "optic"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/optic-accessories/", "optic"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/firearms-accessories/", "misc"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/gun-maintenance/", "misc"));
        webPageEntities.add(create(parent, "http://www.westcoasthunting.ca/product-category/ammunition/", "ammo"));
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
        return "westcoasthunting.ca";
    }
}

