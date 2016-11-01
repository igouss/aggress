package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Copyright NAXSoft 2015
 */
class HicalRawProductParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalRawProductParser.class);

    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("Firearms", "misc");
        mapping.put("Handguns", "misc");
        mapping.put("Rifles - Restricted", "misc");
        mapping.put("Rifles- Non restricted", "misc");
        mapping.put("Rimfire", "misc");
        mapping.put("Shotguns", "misc");
        mapping.put("Used Firearms", "misc");

        mapping.put("Sights & Optics", "optic");
        mapping.put("Binoculars & Spotting Scopes", "optic");
        mapping.put("Optic Accessories", "optic");
        mapping.put("Red/ green dot sights", "optic");
        mapping.put("Scope Rings & Bases", "optic");
        mapping.put("Scopes", "optic");
        mapping.put("Sights", "optic");
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
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
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

            productName = document.select("h2[itemprop='name']").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();
            regularPrice = document.select("#ProductDetails div.DetailRow.PriceRow > div.Value > em").text();
            specialPrice = document.select("#ProductDetails div.Value > strike").text();
            if ("".equals(specialPrice)) {
                regularPrice = parsePrice(webPageEntity, regularPrice);
            } else {
                specialPrice = parsePrice(webPageEntity, specialPrice);
                regularPrice = parsePrice(webPageEntity, regularPrice);
            }
            productImage = document.select("#ProductDetails .ProductThumbImage img").attr("src");
            description = document.select("#ProductDescription").text().trim();
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result);
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
        return "hical.ca";
    }

    @Override
    String getType() {
        return "productPageRaw";
    }

}
