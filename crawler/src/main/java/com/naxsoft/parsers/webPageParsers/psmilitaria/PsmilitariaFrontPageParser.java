package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class PsmilitariaFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaFrontPageParser.class);

    private Collection<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Document document = downloadResult.getDocument();
        Elements elements = document.select("table > tbody > tr > th > a");
        for (Element e : elements) {
            String url = e.attr("abs:href");
            result.add(create(url, e.text()));
        }
        return result;
    }

    public PsmilitariaFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://psmilitaria.50megs.com/guns.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/collectmisc.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/marlin.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/savage.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/remington.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/winchester.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/hunting.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/modpistol.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/pistol.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/mosin.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/antiques.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/oldhunt.html", "firearm"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/ammo.html", "ammo"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/miscel.html", "misc"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/newitems.html", "firearm,misc"));
        webPageEntities.add(create("http://psmilitaria.50megs.com/tools.html", "reload"));
        return Observable.from(webPageEntities)
                .map(webPageEntity -> PageDownloader.download(client, webPageEntity))
                .flatMap(Observable::from)
                .filter(data -> null != data)
                .map(page -> {
                    page.setType("productList");
                    return page;
                });
    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productPage");
        webPageEntity.setCategory(category);
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://psmilitaria.50megs.com/") && webPage.getType().equals("frontPage");
    }
}