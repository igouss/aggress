package com.naxsoft.crawler.parsers.parsers.productParser;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.common.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
class GunshopRawPageParser extends AbstractRawPageParser {
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("/product-category/firearms/", "firearm");
        mapping.put("/product-category/ammunition/", "ammo");
        mapping.put("/product-category/optics/", "optic");
        mapping.put("/product-category/reloading-components/", "reload");
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

            Elements productNameEl = document.select("h1.entry-title");
            if (!productNameEl.isEmpty()) {
                productName = productNameEl.first().text();
                log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());
            } else {
                log.warn("Unable to find product name {}", webPageEntity);
                return result;
            }

            if (!document.select(".entry-summary .out-of-stock").isEmpty()) {
                log.info("Product {} is out of stock. {}", productName, webPageEntity.getUrl());
                return result;
            }

            url = webPageEntity.getUrl();
            productImage = document.select(".wp-post-image").attr("src");
            specialPrice = document.select(".entry-summary .price ins span").text();
            if (specialPrice.isEmpty()) {
                regularPrice = parsePrice(webPageEntity, document.select(".entry-summary .amount").text());
            } else {
                specialPrice = parsePrice(webPageEntity, specialPrice);
                regularPrice = parsePrice(webPageEntity, document.select(".entry-summary del .amount").text());
            }

            description = document.select("#tab-description").text();
            for (Element next : document.select("product_meta span")) {
                String name = next.data();
                if (!name.equalsIgnoreCase("categories")) {
                    Elements values = next.select("a");
                    attr.put(name, values.text());
                }
            }
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        for (String urlPattern : mapping.keySet()) {
            if (webPageEntity.getUrl().contains(urlPattern)) {
                return mapping.get(urlPattern).split(",");
            }
        }
        log.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "gun-shop.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
