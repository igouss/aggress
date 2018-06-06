package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LeverarmsRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeverarmsRawPageParser.class);

    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("Semi-Automatic Rifles", "firearm");
        mapping.put("Bolt Action Rifles", "firearm");
        mapping.put("Lever Action Rifles", "firearm");
        mapping.put("Pistols", "firearm");
        mapping.put("Semi-Automatic Pistols", "firearm");
        mapping.put("Revolvers", "firearm");
        mapping.put("Shotguns", "firearm");
        mapping.put("Mauser K98k", "firearm,misc");
        mapping.put("SKS", "firearm,misc");

        mapping.put("Rifle Ammo", "ammo");
        mapping.put("Pistol Ammo", "ammo");
        mapping.put("Shotgun Ammo", "ammo");
        mapping.put("Rimfire Ammo", "ammo");

        mapping.put("Optics", "optic");

        mapping.put("Used", "firearm");
    }

    public LeverarmsRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

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
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            ProductEntity product;
            String productName = null;
            String url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

            url = webPageEntity.getUrl();

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            productName = document.select(".product-shop .product-name").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select("p.availability.in-stock").isEmpty()) {
                return result;
            }

            productImage = document.select(".product-img-box img").attr("abs:src");
            specialPrice = document.select(".special-price .price").text();
            if (!specialPrice.isEmpty()) {
                regularPrice = parsePrice(webPageEntity, document.select(".old-price .price").text());
                specialPrice = parsePrice(webPageEntity, specialPrice);
            } else {
                regularPrice = parsePrice(webPageEntity, document.select(".regular-price .price").text());
            }

            description = document.select(".product-collateral").text() + " " + document.select(".short-description").text();
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (mapping.containsKey(category)) {
            return mapping.get(category).split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "leverarms.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
