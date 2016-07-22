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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class CorwinArmsProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorwinArmsProductRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("New Arrivals", "misc");
        mapping.put("Firearms", "firearm");
        mapping.put("Firearm Accessories", "optic,misc");
        mapping.put("Magazines", "misc");
        mapping.put("Knives", "misc");
        mapping.put("Flashlights", "misc");
        mapping.put("Axes", "misc");
        mapping.put("Bayonets", "misc");
        mapping.put("Swords", "misc");
        mapping.put("Optics", "optic");
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {

        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            LOGGER.error("failed to parse price {}", price);
            return price;
        }
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws ProductParseException {
        try {
            HashSet<ProductEntity> result = new HashSet<>();

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            String productName = document.select("#maincol h1").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                String img = document.select("div.field-type-image img").attr("abs:src");
                if (img.isEmpty()) {
                    img = document.select("div.field-name-field-product-mag-image > div > div > img").attr("abs:src");
                }
                jsonBuilder.field("productImage", img);
                jsonBuilder.field("regularPrice", parsePrice(document.select(".field-name-commerce-price").text()));
                jsonBuilder.field("description", document.select("div.field-type-text-with-summary > div > div").text().replace("\u0160", "\n"));
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));

                jsonBuilder.endObject();
                product.setUrl(webPageEntity.getUrl());
                product.setJson(jsonBuilder.string());
            }
            product.setWebpageId(webPageEntity.getId());
            result.add(product);

            return result;
        } catch (Exception e) {
            throw new ProductParseException(e);
        }
    }

    /**
     * @param webPageEntity
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        if (mapping.containsKey(webPageEntity.getCategory())) {
            return mapping.get(webPageEntity.getCategory()).split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("corwin-arms.com") && webPage.getType().equals("productPageRaw");
    }
}
