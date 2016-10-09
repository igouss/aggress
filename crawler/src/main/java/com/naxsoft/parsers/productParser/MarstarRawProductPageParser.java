package com.naxsoft.parsers.productParser;

import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.eventbus.Message;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class MarstarRawProductPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarRawProductPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("((\\d+(\\.|,))+\\d\\d)+");

    /**
     * @param webPageEntity
     * @return
     */
    private static String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (null != category) {
            return category.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    /**
     * @param price
     * @return
     */
    private static ArrayList<String> parsePrice(String price) {
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = pricePattern.matcher(price);


        while (matcher.find()) {
            result.add(matcher.group(1).replace(",", ""));
        }
        return result;
    }

    @Override
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            ProductEntity product;
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

                Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

                String productName = document.select("h1").text();
                LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());
                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select("img[id=mainPic]").attr("abs:src"));
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
                ArrayList<String> price = parsePrice(document.select(".priceAvail").text());
                if (price.isEmpty()) {
                    return Observable.empty();
                } else if (1 == price.size()) {
                    jsonBuilder.field("regularPrice", price.get(0));
                } else {
                    jsonBuilder.field("regularPrice", price.get(0));
                    jsonBuilder.field("specialPrice", price.get(1));
                }

                String description = document.select("#main-content > div:nth-child(7)").text();
                if (description.isEmpty()) {
                    description = document.select("#main-content > div:nth-child(6), #main-content > div:nth-child(8)").text();
                }
                jsonBuilder.field("description", description);
                Iterator<Element> labels = document.select("#main-content > table > tbody > tr:nth-child(1) > th").iterator();
                Iterator<Element> values = document.select("#main-content > table > tbody > tr.baseTableCell > td").iterator();

                while (labels.hasNext()) {
                    String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_'));
                    String specValue = values.next().text();
                    jsonBuilder.field(specName, specValue);
                }
                jsonBuilder.endObject();

                product = new ProductEntity(jsonBuilder.string(), webPageEntity.getUrl());
            }
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("marstar.ca") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("marstar.ca/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
