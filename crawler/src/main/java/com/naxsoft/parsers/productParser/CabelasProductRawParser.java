package com.naxsoft.parsers.productParser;

import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class CabelasProductRawParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CabelasProductRawParser.class);
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern priceMatcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("Firearm", "firearm");
        mapping.put("Firearms", "firearm");
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
    public Iterable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            Document document = Jsoup.parse(webPageEntity.getUrl(), 1000);

            ProductEntity product;
            String productName = null;
            URL url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

            productName = document.select("h1.product-heading").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select("link[itemprop=availability]").text().contains("No Longer Available")) {
                LOGGER.info("Product {} no longer available", productName);
                return Set.of();
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
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String s = mapping.get(webPageEntity.getCategory());
        if (null != s) {
            return s.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[] { "misc" };
    }

    @Override
    String getSite() {
        return "cabelas.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
