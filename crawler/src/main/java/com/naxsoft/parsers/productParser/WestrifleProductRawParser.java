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
class WestrifleProductRawParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleProductRawParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("RUSSIAN SKS", "firearm")
            .put("MOSIN NAGANT 91/30", "firearm")
            .put("SHOTGUNS", "firearm")
            .put("AMMUNITIONS", "ammo")
            .put("MOSIN NAGANT PARTS", "misc")
            .put("SKS PARTS", "misc")
            .put("RIFLE SCOPES", "optic")
            .put("ACCESSORIES", "misc")
            .put("BIPODS", "misc")
            .put("MAGAZINES", "misc")
            .put("MOUNTS", "misc")
            .put("MUZZLEBRAKES", "misc")
            .put("STOCKS", "misc")
            .put("COLLECTIBLE MOSIN NAGANT 91/30", "firearm")
            .put("ROCK SOLID MOUNTS", "misc")
            .build();

    public WestrifleProductRawParser(MetricRegistry metricRegistry) {
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

            if (document.select("#productDetailsList > li").text().equals("0 Units in Stock")) {
                return result;
            }

            productName = document.select("#productName").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


            url = webPageEntity.getUrl();
            productImage = document.select("#productMainImage a > img").attr("abs:src");
            description = document.select("#productDescription").text();
            category = getNormalizedCategories(webPageEntity);
            regularPrice = parsePrice(webPageEntity, document.select("#productPrices").text());

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
        if (mapping.containsKey(webPageEntity.getCategory())) {
            return mapping.get(webPageEntity.getCategory()).split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "westrifle.com";
    }
}
