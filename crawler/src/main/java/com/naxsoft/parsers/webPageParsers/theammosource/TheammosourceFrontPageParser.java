package com.naxsoft.parsers.webPageParsers.theammosource;

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
class TheammosourceFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheammosourceFrontPageParser.class);

    public TheammosourceFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".categoryListBoxContents > a");

            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", el.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.trace("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result.build();
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        Set<WebPageEntity> webPageEntities = ImmutableSet.<WebPageEntity>builder()
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=1", "ammo")) // Ammo
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=286", "ammo")) // Ammo
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=2", "firearm")) // FIREARMS
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=166", "firearm")) // FIREARMS
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=520", "firearm")) // FIREARMS
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=340", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=635", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=14", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=207", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=497", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=750", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=373", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=308", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=412", "misc")) // Misc
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=15", "reload")) // reload
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=222", "optic")) // optic
                .add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=21", "optic")) // optic
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
        return "theammosource.com";
    }

}
