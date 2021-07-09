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
public class NordicmarksmanRawPageParser extends AbstractRawPageParser {
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("AR-15", "firearm");
        mapping.put("Shotguns", "firearm");
        mapping.put("Air Rifles", "firearm");
        mapping.put("Barreled Actions", "firearm");
        mapping.put("Benchrest Rifles", "firearm");
        mapping.put("Biathlon Rifles", "firearm");
        mapping.put("Target Rifles", "firearm");
        mapping.put("Hunting Rifles", "firearm");
        mapping.put("MSR Rifles", "firearm");
        mapping.put("Laser Rifles", "firearm");
        mapping.put("Anschutz Rifle Stocks", "misc");
        mapping.put("Biathlon Rifle Stocks", "misc");
        mapping.put("Used/Demo Rifles", "firearm");
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

            if (document.select(".optionstyle").text().contains("This item is currently out of stock.")) {
                return result;
            }

            productName = document.select(".productname").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

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
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(String category) {
        if (mapping.containsKey(category)) {
            return mapping.get(category).split(",");
        }
        log.warn("Unknown category: {}", category);
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "nordicmarksman.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
