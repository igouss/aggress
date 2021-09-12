package com.naxsoft.crawler.parsers.parsers.productParser;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.common.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
class FishingWorldRawPageParser extends AbstractRawPageParser {
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

            productName = document.select("#product > h1").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select("#details div.warning").text().equals("Out of Stock")) {
                return result;
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
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (null != category) {
            return category.split(",");
        }
        log.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
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
