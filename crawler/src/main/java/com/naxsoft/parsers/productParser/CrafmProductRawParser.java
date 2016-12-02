package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class CrafmProductRawParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmProductRawParser.class);
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("Competition Accessories", "misc")
            .put("Firearms", "firearm")
            .put("Batteries", "misc")
            .put("Magazines", "misc")
            .put("Chronographs", "misc")
            .put("Targets", "misc")
            .put("Safes & Cases", "misc")
            .put("Knives & Tools", "misc")
            .put("Miscellaneous", "misc")
            .put("Lights & Lasers", "misc")
            .put("Books & DVD's", "misc")
            .put("Mounts & Rings", "misc")
            .put("Ammunition", "ammo")
            .put("Parts & Accessories", "misc")
            .put("Grips", "misc")
            .put("Cleaning Products", "misc")
            .put("Protection", "misc")
            .put("Reloading", "reload")
            .put("Scopes& Opticals", "optic")
            .put("Clothing", "misc")
            .put("LIQUIDATION", "misc")
            .put("PROMOTIONS", "misc")
            .build();
    private static final Pattern pricePattern = Pattern.compile("((\\d+|,)+\\.\\d+)");

    public CrafmProductRawParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            LOGGER.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return price;
        }
    }

    @Override
    public Collection<ProductEntity> parse(WebPageEntity webPageEntity) {
        ImmutableSet.Builder<ProductEntity> result = ImmutableSet.builder();
        try {
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            ProductEntity product;
            String productName;
            String url;
            String regularPrice;
            String specialPrice = null;
            String productImage;
            String description;
            Map<String, String> attr = new HashMap<>();
            String[] category;

            productName = document.select(".product-essential .product-name").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();
            productImage = document.select(".product-image img").attr("src");
            regularPrice = parsePrice(webPageEntity, document.select("#product_addtocart_form > div.product-shop > div:nth-child(4) > h2 > span").text());
            description = document.select("div.short-description p[align=justify]").text();
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return result.build();
    }

    /**
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
    String getSite() {
        return "crafm.com";
    }
}
