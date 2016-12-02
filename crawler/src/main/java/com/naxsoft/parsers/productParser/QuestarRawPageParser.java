package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class QuestarRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestarRawPageParser.class);
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("Firearms-Handguns", "firearm")
            .put("Firearms-Long Guns", "firearm")
            .put("Sights/Lasers/Scopes", "optic")
            .build();

    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    public QuestarRawPageParser(MetricRegistry metricRegistry) {
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
        HashSet<ProductEntity> result = new HashSet<>();

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

            productName = document.select("#main > table > tbody > tr:nth-child(1) > td > table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(1)").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


            url = webPageEntity.getUrl();
            productImage = document.select("a[rel=lightbox] img").attr("abs:src");
            description = document.select("#main > table > tbody > tr:nth-child(2) > td").text();
            category = getNormalizedCategories(webPageEntity);
            regularPrice = parsePrice(webPageEntity, document.select("#main td > span.price").text());

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
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
        return "shopquestar.com";
    }
}