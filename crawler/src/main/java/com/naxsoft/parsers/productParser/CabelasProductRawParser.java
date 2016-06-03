package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class CabelasProductRawParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CabelasProductRawParser.class);
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Firearm", "firearm");
        mapping.put("Ammunition", "ammo");
        mapping.put("Optics", "optic");
        mapping.put("Firearm Accessories", "misc");
        mapping.put("Range Accessories", "misc");
        mapping.put("Reloading", "reloading");
        mapping.put("Airguns", "firearm");
        mapping.put("Gun Cases & Storage", "misc");
        mapping.put("Firearm Care", "misc");
        mapping.put("Muzzleloading", "firearm");
        mapping.put("Airsoft", "misc");
    }

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
            LOGGER.error("failed to parse price {}", price);
            return price;
        }
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws ProductParseException {
        try {
            HashSet<ProductEntity> result = new HashSet<>();

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            String productName = document.select("h1.product-heading").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select("#product-image img").attr("src"));

                Elements specialPrice = document.select(".productDetails-secondary .price-secondary");
                Elements regularPrice = document.select(".productDetails-secondary .price-primary");
                if (!specialPrice.isEmpty()) {
                    jsonBuilder.field("regularPrice", parsePrice(regularPrice.text()));
                    jsonBuilder.field("specialPrice", parsePrice(specialPrice.text()));
                } else {
                    jsonBuilder.field("regularPrice", parsePrice(regularPrice.text()));
                }
                jsonBuilder.field("description", document.select(".productDetails-section .row").text());
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
     *
     * @param webPageEntity
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String s = mapping.get(webPageEntity.getCategory());
        if (null != s) {
            return s.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productPageRaw");
    }
}
