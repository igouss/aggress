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

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class CabelasProductRawParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CabelasProductRawParser.class);
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("Firearm", "firearm")
            .put("Firearms", "firearm")
            .put("Ammunition", "ammo")
            .put("Optics", "optic")
            .put("Firearm Accessories", "misc")
            .put("Range Accessories", "misc")
            .put("Reloading", "reloading")
            .put("Airguns", "firearm")
            .put("Gun Cases & Storage", "misc")
            .put("Firearm Care", "misc")
            .put("Muzzleloading", "firearm")
            .put("Airsoft", "misc")
            .build();
    private static final Pattern priceMatcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");


    public CabelasProductRawParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = priceMatcher.matcher(price);
        if (matcher.find()) {
            try {
                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                return Double.valueOf(matcher.group(1)).toString();
            }
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
            String specialPrice;
            String productImage;
            String description;
            Map<String, String> attr = new HashMap<>();
            String[] category;

            productName = document.select("h1.product-heading").text();
            LOGGER.trace("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select("link[itemprop=availability]").text().contains("No Longer Available")) {
                LOGGER.trace("Product {} no longer available", productName);
                return result.build();
            }

            url = webPageEntity.getUrl();
            productImage = document.select("#product-image img").attr("src");

            specialPrice = document.select(".productDetails-secondary .price-secondary").text();
            regularPrice = document.select(".productDetails-secondary .price-primary").text();
            if (!specialPrice.isEmpty()) {
                regularPrice = parsePrice(webPageEntity, regularPrice);
                specialPrice = parsePrice(webPageEntity, specialPrice);
            } else {
                regularPrice = parsePrice(webPageEntity, regularPrice);
            }
            description = document.select(".productDetails-section .row").text();
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
        return "cabelas.ca";
    }

}
