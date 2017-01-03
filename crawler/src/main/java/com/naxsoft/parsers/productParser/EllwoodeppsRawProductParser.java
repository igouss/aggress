package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class EllwoodeppsRawProductParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsRawProductParser.class);
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("firearm", "firearm")
            .put("Ammo", "ammo")
            .put("Archery Accessories", "misc")
            .put("Barrels", "misc")
            .put("Bayonets", "misc")
            .put("Binoculars", "misc")
            .put("Bipods", "optic")
            .put("Books, Manuals and Videos", "misc")
            .put("Chokes", "misc")
            .put("Firearm Cleaning &Tools ", "misc")
            .put("Grips", "misc")
            .put("Gun Cases", "misc")
            .put("Holsters", "misc")
            .put("Knives", "misc")
            .put("Magazines", "misc")
            .put("Militaria", "misc")
            .put("Misc Gun Parts", "misc")
            .put("Misc Hunting Accessories", "misc")
            .put("Reloading", "reload")
            .put("Scopes & Mounts", "optic")
            .put("Shooting Accessories", "misc")
            .put("Stocks", "misc")
            .build();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    public EllwoodeppsRawProductParser(MetricRegistry metricRegistry) {
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
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category;

            if (!document.select(".firearm-links-sold").isEmpty()) {
                return result.build();
            }

            productName = document.select(".product-name span").text();
            LOGGER.trace("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();
            if (!document.select(".old-price").isEmpty()) {
                specialPrice = parsePrice(webPageEntity, document.select(".special-price .price").text());
                regularPrice = parsePrice(webPageEntity, document.select(".old-price .price").text());
            } else {
                regularPrice = parsePrice(webPageEntity, document.select(".price").text());
            }
            Iterator<Element> labels = document.select("th.label").iterator();
            Iterator<Element> values = document.select("td.data").iterator();

            while (labels.hasNext()) {
                String specName = labels.next().text();
                String specValue = values.next().text();
                if (!specValue.isEmpty()) {
                    attr.put(specName, specValue);
                }
            }
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
        return "ellwoodepps.com";
    }
}
