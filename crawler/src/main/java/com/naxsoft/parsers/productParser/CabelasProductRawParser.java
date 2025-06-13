package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    public Flux<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            ProductEntity product;
            String productName = null;
            String url = null;
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
                return Flux.empty();
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
            product = ProductEntity.legacyCreate(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Flux.fromIterable(result)
                .doOnNext(e -> parseResultCounter.inc());
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

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
