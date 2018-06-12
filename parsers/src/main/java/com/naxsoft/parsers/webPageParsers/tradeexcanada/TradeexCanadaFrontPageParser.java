package com.naxsoft.parsers.webPageParsers.tradeexcanada;

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

class TradeexCanadaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaFrontPageParser.class);

    public TradeexCanadaFrontPageParser(HttpClient client) {
        super(client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url) {
        return new WebPageEntity(parent, "", "productList", url, "N/A");
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".view-content a");
            for (Element element : elements) {
                String category;
                if (element.text().contains("AAA Super Specials")) {
                    category = "firearm,ammo";
                } else if (element.text().contains("Combination Guns")) {
                    category = "firearm";
                } else if (element.text().contains("Double Rifles")) {
                    category = "firearm";
                } else if (element.text().contains("Handguns")) {
                    category = "firearm";
                } else if (element.text().contains("Hunting and Sporting Arms")) {
                    category = "firearm";
                } else if (element.text().contains("Rifle")) {
                    category = "firearm";
                } else if (element.text().contains("Shotguns")) {
                    category = "firearm";
                } else if (element.text().contains("Ammunition")) {
                    category = "ammo";
                } else if (element.text().contains("Reloading Components")) {
                    category = "reload";
                } else if (element.text().contains("Scopes")) {
                    category = "optic";
                } else {
                    category = "misc";
                }

                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", element.attr("abs:href"), category);
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "https://www.tradeexcanada.com/products_list"));

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
        return "tradeexcanada.com";
    }
}