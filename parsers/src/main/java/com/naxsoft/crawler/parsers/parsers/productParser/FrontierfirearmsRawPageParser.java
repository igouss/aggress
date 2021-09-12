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
class FrontierfirearmsRawPageParser extends AbstractRawPageParser {
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

            if (!document.select(".firearm-links-sold").isEmpty()) {
                return result;
            }

            productName = document.select(".product-name h1").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


            url = webPageEntity.getUrl();
            productImage = document.select(".product-img-box img").attr("src");
            specialPrice = document.select(".ProductPrice.retail-product-price").text();
            if (specialPrice.isEmpty()) {
                regularPrice = parsePrice(webPageEntity, document.select(".ProductPrice.VariationProductPrice").text());
            } else {
                specialPrice = parsePrice(webPageEntity, specialPrice);
                regularPrice = parsePrice(webPageEntity, document.select(".ProductPrice.VariationProductPrice").text());
            }
            //        jsonBuilder.field("description", document.select(".short-description").text());
            description = document.select("#product_tabs_description_tabbed_contents > div").text();
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
        return "frontierfirearms.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
