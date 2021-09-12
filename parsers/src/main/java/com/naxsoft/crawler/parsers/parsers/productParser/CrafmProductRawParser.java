package com.naxsoft.crawler.parsers.parsers.productParser;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.common.entity.WebPageEntity;
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
class CrafmProductRawParser extends AbstractRawPageParser {
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("Competition Accessories", "misc");
        mapping.put("Firearms", "firearm");
        mapping.put("Batteries", "misc");
        mapping.put("Magazines", "misc");
        mapping.put("Chronographs", "misc");
        mapping.put("Targets", "misc");
        mapping.put("Safes & Cases", "misc");
        mapping.put("Knives & Tools", "misc");
        mapping.put("Miscellaneous", "misc");
        mapping.put("Lights & Lasers", "misc");
        mapping.put("Books & DVD's", "misc");
        mapping.put("Mounts & Rings", "misc");
        mapping.put("Ammunition", "ammo");
        mapping.put("Parts & Accessories", "misc");
        mapping.put("Grips", "misc");
        mapping.put("Cleaning Products", "misc");
        mapping.put("Protection", "misc");
        mapping.put("Reloading", "reload");
        mapping.put("Scopes& Opticals", "optic");
        mapping.put("Clothing", "misc");
        mapping.put("LIQUIDATION", "misc");
        mapping.put("PROMOTIONS", "misc");
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

            productName = document.select(".product-essential .product-name").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();
            productImage = document.select(".product-image img").attr("src");
            regularPrice = parsePrice(webPageEntity, document.select("#product_addtocart_form > div.product-shop > div:nth-child(4) > h2 > span").text());
            description = document.select("div.short-description p[align=justify]").text();
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String s = mapping.get(webPageEntity.getCategory());
        if (null != s) {
            return s.split(",");
        }
        log.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "crafm.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
