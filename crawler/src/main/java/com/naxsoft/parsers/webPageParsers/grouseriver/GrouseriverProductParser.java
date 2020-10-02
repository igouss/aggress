package com.naxsoft.parsers.webPageParsers.grouseriver;

import java.net.URL;
import java.util.HashSet;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrouseriverProductParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrouseriverProductParser.class);

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity parent) throws Exception {
        HashSet<WebPageEntity> result = new HashSet<>();
        Document parsedJson = Jsoup.parse(parent.getUrl(), 1000);
        Elements items = parsedJson.select("items");
        for (Element itemData : items) {
            LOGGER.info("Processing: " + itemData.text("displayname"));
            result.add(new WebPageEntity(
                    parent,
                    "productPageRaw",
                    new URL("http://www.grouseriver.com/" + itemData.select("urlcomponent").text()),
                    parent.getCategory()));
        }
        return result;
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "grouseriver.com";
    }
}
