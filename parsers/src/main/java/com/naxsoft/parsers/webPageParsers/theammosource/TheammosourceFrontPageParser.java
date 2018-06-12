package com.naxsoft.parsers.webPageParsers.theammosource;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DocumentCompletionHandler;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TheammosourceFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheammosourceFrontPageParser.class);

    public TheammosourceFrontPageParser(HttpClient client) {
        super(client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
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
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
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
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from).toList().toBlocking().single();
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
