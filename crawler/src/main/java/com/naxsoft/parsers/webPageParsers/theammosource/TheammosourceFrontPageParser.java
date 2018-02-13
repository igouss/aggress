package com.naxsoft.parsers.webPageParsers.theammosource;

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
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.HashSet;
import java.util.Set;

class TheammosourceFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheammosourceFrontPageParser.class);

    public TheammosourceFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".categoryListBoxContents > a");

            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", el.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=1", "ammo")); // Ammo
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=286", "ammo")); // Ammo
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=2", "firearm")); // FIREARMS
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=166", "firearm")); // FIREARMS
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=520", "firearm")); // FIREARMS
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=340", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=635", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=14", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=207", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=497", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=750", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=373", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=308", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=412", "misc")); // Misc
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=15", "reload")); // reload
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=222", "optic")); // optic
        webPageEntities.add(create(parent, "http://www.theammosource.com/index.php?main_page=index&cPath=21", "optic")); // optic

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
        return "theammosource.com";
    }
}
