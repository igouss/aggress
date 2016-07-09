package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class FishingWorldRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FishingWorldRawPageParser.class);

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            try {
                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                return Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            return price;
        }
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws ProductParseException {
        try {
            HashSet<ProductEntity> products = new HashSet<>();
            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

                Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

                String productName = document.select("#product > h1").text();
                LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

                if (document.select("#details div.warning").text().equals("Out of Stock")) {
                    return products;
                }

                jsonBuilder.field("productName", productName);

                jsonBuilder.field("productImage", document.select(".product-image").attr("abs:src"));
                String priceBox = document.select("#details > div.column1.float-right > div.model").text();
                if ("Regular".contains(priceBox)) {
                    jsonBuilder.field("regularPrice", parsePrice(document.select("#details > div.column1.float-right > div.model").text()));
                    jsonBuilder.field("specialPrice", parsePrice(document.select("#details > div.column1.float-right > div > div.price.blue").text()));
                } else {
                    jsonBuilder.field("regularPrice", parsePrice(document.select("#details > div.column1.float-right > div > div.price.blue").text()));
                }

                jsonBuilder.field("description", document.select("#details > div.column2.float-left").text());
                String allCategories = webPageEntity.getCategory();
                if (allCategories != null) {
                    jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
                }
                jsonBuilder.endObject();
                product.setUrl(webPageEntity.getUrl());
                product.setWebpageId(webPageEntity.getId());
                product.setJson(jsonBuilder.string());
            }
            products.add(product);
            return products;
        } catch (Exception e) {
            throw new ProductParseException(e);
        }
    }

    /**
     * @param webPageEntity
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (null != category) {
            return category.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("fishingworld.ca") && webPage.getType().equals("productPageRaw");
    }
}
