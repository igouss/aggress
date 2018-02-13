package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PsmilitariaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaFrontPageParser.class);

    public PsmilitariaFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Collection<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("table > tbody > tr > th > a");
            for (Element e : elements) {
                String url = e.attr("abs:href");
                WebPageEntity webPageEntity = create(downloadResult.getSourcePage(), url, e.text());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        LOGGER.info("Parsing psmilitaria front-page");
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/guns.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/collectmisc.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/marlin.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/savage.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/remington.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/winchester.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/hunting.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/modpistol.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/pistol.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/mosin.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/antiques.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/oldhunt.html", "firearm"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/ammo.html", "ammo"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/miscel.html", "misc"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/newitems.html", "firearm,misc"));
        webPageEntities.add(create(parent, "http://psmilitaria.50megs.com/tools.html", "reload"));
        return Observable.from(webPageEntities)
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