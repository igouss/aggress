package com.naxsoft.parsers.webPageParsers.alflahertys;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class AlflahertysFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlflahertysFrontPageParser.class);

    private final HttpClient client;

    public AlflahertysFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Document document = downloadResult.getDocument();
        Elements elements = document.select("ul.main.menu a");

        Set<String> validCategories = new HashSet<>();
        validCategories.add("FIREARMS");
        validCategories.add("HANDGUNS");
        validCategories.add("RESTRICTED RIFLES");
        validCategories.add("RIFLES");
        validCategories.add("SHOTGUNS");
        validCategories.add("BLACK POWDER");


        validCategories.add("HANDGUN AMMUNITION");
        validCategories.add("BULK RIFLE AMMO");
        validCategories.add("RIFLE AMMO");
        validCategories.add("RIMFIRE AMMUNTION");
        validCategories.add("SHOTGUN AMMO");
        validCategories.add("RELOADING");

        validCategories.add("SCOPES");
        validCategories.add("CLOSE QUARTERS OPTICS & IRON SIGHTS");
        validCategories.add("RANGE FINDERS");
        validCategories.add("SPOTTING SCOPES");
        validCategories.add("BINOCULARS");
        validCategories.add("OPTIC CARE");
        validCategories.add("OPTIC MOUNTS");
        validCategories.add("NIGHT VISION");
        validCategories.add("SIGHTING TOOLS");

        validCategories.add("HANDGUN CASES");
        validCategories.add("SOFT CASES");
        validCategories.add("HARD CASES");
        validCategories.add("RANGE BAGS");
        validCategories.add("AMMUNITION STORAGE");
        validCategories.add("CABINETS & SAFES");
        validCategories.add("SAFE ACCESSORIES");
        validCategories.add("LOCKS");

        validCategories.add("HOLSTERS, MAG POUCHES, & SHELL HOLDERS");
        validCategories.add("LIGHTS & LASERS");
        validCategories.add("RAILS & MOUNTS");
        validCategories.add("UTILITY BAGS & PACKS");
        validCategories.add("TACTICAL TOOLS");

        validCategories.add("AR COMPONENTS");
        validCategories.add("GRIPS");
        validCategories.add("RIFLE PARTS & STOCKS");
        validCategories.add("HANDGUN PARTS");
        validCategories.add("SHOTGUN PARTS & STOCKS");
        validCategories.add("SHOTGUN BARRELS & CHOKES");
        validCategories.add("CONVERSION KITS");

        validCategories.add("FIREARM MAINTENANCE & TOOLS");
        validCategories.add("BIPODS AND SHOOTING RESTS");
        validCategories.add("SLINGS & SWIVELS");
        validCategories.add("EYES & EARS");
        validCategories.add("CLIPS & MAGAZINES");
        validCategories.add("SHOTGUN ACCESSORIES");
        validCategories.add("TARGETS");

        validCategories.add("ACCESSORIES");
        validCategories.add("FIELD DRESSING & TOOLS");
        validCategories.add("GAME CALLS DECOYS & ACCESSORIES");
        validCategories.add("SCENTS, DETERGENTS, & ATTRACTANTS");
        validCategories.add("BLINDS & CAMOUFLAGE");
        validCategories.add("TRAIL CAMERAS");
        validCategories.add("HUNTING CLOTHES");

        for (Element e : elements) {
            if (e.attr("href").endsWith("#")) {
                continue;
            }
            if (!validCategories.contains(e.text().toUpperCase())) {
                LOGGER.info("Ignoring category: " + e.text() + " " + e.attr("abs:href"));
                continue;
            }
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, e.attr("abs:href"), e.text());
            LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
            result.add(webPageEntity);
        }
        return result;
    }

    private Collection<WebPageEntity> parseProductPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Document document = downloadResult.getDocument();
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
            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, downloadResult.getSourcePage().getUrl(), downloadResult.getSourcePage().getCategory());
            LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
            result.add(webPageEntity);
        } else {
            for (int i = 1; i <= max; i++) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, downloadResult.getSourcePage().getUrl() + "?page=" + i, downloadResult.getSourcePage().getCategory());
                LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.from(client.get(parent.getUrl(), new DocumentCompletionHandler(parent)), Schedulers.io())
                .map(this::parseFrontPage)
                .flatMap(Observable::from)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseProductPage)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("frontPage");
    }
}
