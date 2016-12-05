package com.naxsoft.parsers.webPageParsers.alflahertys;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.reactivex.Flowable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class AlflahertysFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlflahertysFrontPageParser.class);
    private final static Set<String> validCategories = ImmutableSet.<String>builder()
            .add("HANDGUNS")
            .add("RESTRICTED RIFLES")
            .add("RIFLES")
            .add("SHOTGUNS")
            .add("BLACK POWDER")
            .add("AIRGUNS")
            .add("HANDGUN AMMUNITION")
            .add("BULK RIFLE AMMO")
            .add("RIFLE AMMO")
            .add("RIMFIRE AMMUNTION")
            .add("SHOTGUN AMMO")
            .add("RELOADING")
            .add("SCOPES")
            .add("CLOSE QUARTERS OPTICS & IRON SIGHTS")
            .add("RANGE FINDERS")
            .add("SPOTTING SCOPES")
            .add("BINOCULARS")
            .add("OPTIC CARE")
            .add("OPTIC MOUNTS")
            .add("NIGHT VISION")
            .add("SIGHTING TOOLS")
            .add("HANDGUN CASES")
            .add("SOFT CASES")
            .add("HARD CASES")
            .add("RANGE BAGS")
            .add("AMMUNITION STORAGE")
            .add("CABINETS & SAFES")
            .add("SAFE ACCESSORIES")
            .add("LOCKS")
            .add("HOLSTERS, MAG POUCHES, & SHELL HOLDERS")
            .add("LIGHTS & LASERS")
            .add("RAILS & MOUNTS")
            .add("UTILITY BAGS & PACKS")
            .add("TACTICAL TOOLS")
            .add("AR COMPONENTS")
            .add("GRIPS")
            .add("RIFLE PARTS & STOCKS")
            .add("HANDGUN PARTS")
            .add("SHOTGUN PARTS & STOCKS")
            .add("SHOTGUN BARRELS & CHOKES")
            .add("CONVERSION KITS")
            .add("FIREARM MAINTENANCE & TOOLS")
            .add("BIPODS AND SHOOTING RESTS")
            .add("SLINGS & SWIVELS")
            .add("EYES & EARS")
            .add("CLIPS & MAGAZINES")
            .add("SHOTGUN ACCESSORIES")
            .add("TARGETS")
            .add("ACCESSORIES")
            .add("FIELD DRESSING & TOOLS")
            .add("GAME CALLS DECOYS & ACCESSORIES")
            .add("SCENTS, DETERGENTS, & ATTRACTANTS")
            .add("BLINDS & CAMOUFLAGE")
            .add("TRAIL CAMERAS")
            .add("HUNTING CLOTHES")
            .build();

    public AlflahertysFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Flowable<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("ul.main.menu a");


            for (Element e : elements) {
                if (e.attr("href").endsWith("#")) {
                    continue;
                }
                if (!validCategories.contains(e.text().toUpperCase())) {
                    LOGGER.info("Ignoring category: " + e.text() + " " + e.attr("abs:href"));
                    continue;
                }
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", e.attr("abs:href"), e.text());
                LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
                result.add(webPageEntity);
            }
        }
        return Flowable.fromIterable(result);
    }

    private Flowable<WebPageEntity> parseProductPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".paginate a");
            int max = 0;
            for (Element element : elements) {
                try {
                    int tmp = Integer.parseInt(element.text());
                    if (tmp > max) {
                        max = tmp;
                    }
                } catch (NumberFormatException ignore) {
                    // ignore
                }
            }
            if (max == 0) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", downloadResult.getSourcePage().getUrl(), downloadResult.getSourcePage().getCategory());
                LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
                result.add(webPageEntity);
            } else {
                for (int i = 1; i <= max; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", downloadResult.getSourcePage().getUrl() + "?page=" + i, downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
                    result.add(webPageEntity);
                }
            }
        }
        return Flowable.fromIterable(result);
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        return client.get(parent.getUrl(), new DocumentCompletionHandler(parent))
                .flatMap(this::parseFrontPage)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseProductPage)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "alflahertys.com";
    }
}
