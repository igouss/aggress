package com.naxsoft.parsers.productParser;

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
class FishingWorldRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FishingWorldRawPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    private static String parsePrice(String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            try {
                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                return Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            return price;
        }
    }

    @Override
    public Iterable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            ProductEntity product;
            String productName = null;
            URL url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

            url = webPageEntity.getUrl();
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            productName = document.select("#product > h1").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select("#details div.warning").text().equals("Out of Stock")) {
                return Set.of();
            }

            productImage = document.select(".product-image").attr("abs:src");
            String priceBox = document.select("#details > div.column1.float-right > div.model").text();
            if ("Regular".contains(priceBox)) {
                regularPrice = parsePrice(document.select("#details > div.column1.float-right > div.model").text());
                specialPrice = parsePrice(document.select("#details > div.column1.float-right > div > div.price.blue").text());
            } else {
                regularPrice = parsePrice(document.select("#details > div.column1.float-right > div > div.price.blue").text());
            }

            description = document.select("#details > div.column2.float-left").text();
            String allCategories = webPageEntity.getCategory();
            if (allCategories != null) {
                category = getNormalizedCategories(webPageEntity);
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
        return "fishingworld.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
