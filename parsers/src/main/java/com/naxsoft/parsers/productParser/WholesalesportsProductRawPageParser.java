package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
class WholesalesportsProductRawPageParser extends AbstractRawPageParser {
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

            productName = document.select("h1.product-name").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (2 == document.select("div.alert.negative").size()) {
                return result;
            }

            url = webPageEntity.getUrl();
            productImage = document.select(".productImagePrimaryLink img").attr("abs:src");
            attr.put("manufacturer", document.select(".product-brand").text());
            category = getNormalizedCategories(webPageEntity);
            if (document.select(".new .price-value").isEmpty()) {
                Elements price = document.select(".current .price-value");
                if (!price.isEmpty()) {
                    regularPrice = parsePrice(webPageEntity, price.text());
                } else {
                    price = document.select("div.productDescription span.price-value");
                    regularPrice = parsePrice(webPageEntity, price.text());
                }
            } else {
                regularPrice = parsePrice(webPageEntity, document.select(".old .price-value").text());
                specialPrice = parsePrice(webPageEntity, document.select(".new .price-value").text());
            }
            description = document.select(".summary").text();

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
        return "wholesalesports.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
