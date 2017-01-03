package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
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
class PsmilitariaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaFrontPageParser.class);

    public PsmilitariaFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("table > tbody > tr > th > a");
            for (Element e : elements) {
                String url = e.attr("abs:href");
                WebPageEntity webPageEntity = create(downloadResult.getSourcePage(), url, e.text());
                result.add(webPageEntity);
            }
        }
        return result.build();
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        LOGGER.trace("Parsing psmilitaria front-page");
        Set<WebPageEntity> webPageEntities = ImmutableSet.<WebPageEntity>builder()
                .add(create(parent, "http://psmilitaria.50megs.com/guns.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/collectmisc.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/marlin.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/savage.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/remington.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/winchester.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/hunting.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/modpistol.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/pistol.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/mosin.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/antiques.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/oldhunt.html", "firearm"))
                .add(create(parent, "http://psmilitaria.50megs.com/ammo.html", "ammo"))
                .add(create(parent, "http://psmilitaria.50megs.com/miscel.html", "misc"))
                .add(create(parent, "http://psmilitaria.50megs.com/newitems.html", "firearm,misc"))
                .add(create(parent, "http://psmilitaria.50megs.com/tools.html", "reload"))
                .build();
        return Flowable.fromIterable(webPageEntities)
                .observeOn(Schedulers.io())
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "psmilitaria.50megs.com";
    }

}