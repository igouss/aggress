package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
class MagnumgunsRawPageParser extends AbstractRawPageParser {
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("Left Handed", "firearm");
        mapping.put("Air Guns", "firearm");
        mapping.put("Optics", "optic");
        mapping.put("Pistols", "firearm");
        mapping.put("Rifles", "firearm");
        mapping.put("Safes", "misc");
        mapping.put("Shotguns", "firearm");
        mapping.put("Youth", "firearm");
    }

    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            log.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
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

            productName = document.select(".product_title").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


            if (document.select(".stock.out-of-stock").size() != 0) {
                log.info("Product {} out of stock", productName);
                return result;
            }

            productImage = document.select(".wp-post-image").attr("abs:src");
            regularPrice = document.select("meta[itemprop=price]").attr("content");
            description = document.select("div[itemprop=description]").text();
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        for (String categoryName : mapping.keySet()) {
            if (webPageEntity.getCategory().contains(categoryName)) {
                return mapping.get(categoryName).split(",");
            }
        }
        log.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "magnumguns.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
