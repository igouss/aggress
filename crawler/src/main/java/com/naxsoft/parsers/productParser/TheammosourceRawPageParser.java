package com.naxsoft.parsers.productParser;

import java.util.HashMap;
import java.util.HashSet;
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
class TheammosourceRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheammosourceRawPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

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
    public Iterable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            ProductEntity product;
            String productName = null;
            URL url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

            productName = document.select("#productListHeading").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select("#productDetailsList > li:nth-child(2)").text().equals("0 Units in Stock")) {
                return Set.of();
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

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (null != category) {
            return category.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[] { "misc" };
    }

    @Override
    String getSite() {
        return "theammosource.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}