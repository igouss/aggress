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
public class WolverinegtRawPageParser extends AbstractRawPageParser {
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

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

            productName = document.select(".entry-summary h1").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (!document.select(".stock.out-of-stock").isEmpty()) {
                return result;
            }


            url = webPageEntity.getUrl();


            productImage = document.select("div.images img").attr("abs:src");
//                jsonBuilder.field("manufacturer", document.select(".product-brand").text());
            category = getNormalizedCategories(webPageEntity);

            String price = document.select(".entry-summary  .price > .amount").text();
            if (price.isEmpty()) {
                regularPrice = parsePrice(webPageEntity, document.select(".entry-summary .price del .amount").text());
                specialPrice = parsePrice(webPageEntity, document.select(".entry-summary .price ins .amount").text());
            } else {
                regularPrice = parsePrice(webPageEntity, price);
            }

            description = document.select(".entry-summary  .description").text();
            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        return webPageEntity.getCategory().split(",");
    }

    @Override
    String getSite() {
        return "wolverinegt.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
