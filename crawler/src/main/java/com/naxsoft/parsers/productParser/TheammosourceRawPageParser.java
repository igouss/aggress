package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
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
class TheammosourceRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheammosourceRawPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    public TheammosourceRawPageParser(MetricRegistry metricRegistry) {
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
            String specialPrice;
            String productImage;
            String description;
            Map<String, String> attr = new HashMap<>();
            String[] category;

            productName = document.select("#productListHeading").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select("#productDetailsList > li:nth-child(2)").text().equals("0 Units in Stock")) {
                return result;
            }


            url = webPageEntity.getUrl();
            productImage = document.select("#productMainImage img").attr("abs:src");
            description = document.select("#productDescription").text();
            category = getNormalizedCategories(webPageEntity);
            specialPrice = document.select("#productPrices .productSpecialPrice").text();
            if (!specialPrice.isEmpty()) {
                regularPrice = parsePrice(webPageEntity, document.select("#productPrices .normalprice").text());
                specialPrice = parsePrice(webPageEntity, specialPrice);
            } else {
                regularPrice = parsePrice(webPageEntity, document.select("#productPrices #retail").text());
            }

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
        String category = webPageEntity.getCategory();
        if (null != category) {
            return category.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "theammosource.com";
    }
}