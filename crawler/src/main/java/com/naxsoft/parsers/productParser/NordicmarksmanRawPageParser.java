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

class NordicmarksmanRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleProductRawParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("AR-15", "firearm")
            .put("Shotguns", "firearm")
            .put("Air Rifles", "firearm")
            .put("Barreled Actions", "firearm")
            .put("Benchrest Rifles", "firearm")
            .put("Biathlon Rifles", "firearm")
            .put("Target Rifles", "firearm")
            .put("Hunting Rifles", "firearm")
            .put("MSR Rifles", "firearm")
            .put("Laser Rifles", "firearm")
            .put("Anschutz Rifle Stocks", "misc")
            .put("Biathlon Rifle Stocks", "misc")
            .put("Used/Demo Rifles", "firearm")
            .build();


    public NordicmarksmanRawPageParser(MetricRegistry metricRegistry) {
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

            if (document.select(".optionstyle").text().contains("This item is currently out of stock.")) {
                return result.build();
            }

            productName = document.select(".productname").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


            url = webPageEntity.getUrl();
            productImage = document.select("img[name='altimage']").attr("abs:src");
            description = document.select(".producttabcontent").text();
            String originalPrice = document.select(".productpage .productmsrp strike").text();
            if (originalPrice.isEmpty()) {
                originalPrice = document.select(".productpage .productunitprice").text();
                regularPrice = parsePrice(webPageEntity, originalPrice);
            } else {
                specialPrice = document.select(".productpage .productunitprice").text();
                originalPrice = document.select(".productpage .productmsrp strike").text();

                regularPrice = parsePrice(webPageEntity, originalPrice);
                specialPrice = parsePrice(webPageEntity, specialPrice);
            }
            category = getNormalizedCategories(webPageEntity.getCategory());

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return result.build();
    }

    /**
     * @param category
     * @return
     */
    private String[] getNormalizedCategories(String category) {
        if (mapping.containsKey(category)) {
            return mapping.get(category).split(",");
        }
        LOGGER.warn("Unknown category: {}", category);
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "nordicmarksman.com";
    }
}
