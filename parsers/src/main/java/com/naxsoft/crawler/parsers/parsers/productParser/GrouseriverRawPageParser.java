package com.naxsoft.crawler.parsers.parsers.productParser;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.common.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GrouseriverRawPageParser extends AbstractRawPageParser {
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("firearm", "firearm");
        mapping.put("optic", "optic");
    }

    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            try {
                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                return Double.valueOf(matcher.group(1)).toString();
            }
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
            productName = document.select("#itemDetailsHeader").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            Elements productImageElement = document.select("div.item-image-gallery li:nth-child(1) img");
            if (!productImageElement.isEmpty()) {
                productImage = productImageElement.attr("abs:src");
            } else {
                productImage = document.select(".item-detailed-page .item-detailed-image img").attr("abs:src");
            }

            String price = document.select("span[itemprop=lowPrice]").text();
            if (price.isEmpty()) {
                price = document.select(".lead-price").text();
            }
            if (document.select(".lead .crossed").isEmpty()) {
                regularPrice = parsePrice(webPageEntity, price);
            } else {
                regularPrice = parsePrice(webPageEntity, document.select(".lead .crossed").text());
                specialPrice = parsePrice(webPageEntity, document.select(".lead-price").text());
            }
            String desc1 = document.select("div[itemprop=description]").text();
            String desc2 = document.select("#customInfo").text();
            String desc3 = document.select("#customInfo2").text();
            description = desc1 + "\n" + desc2 + "\n" + desc3;
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
        return "grouseriver.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
