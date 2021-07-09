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

@Slf4j
class InternationalshootingsuppliesRawPageParser extends AbstractRawPageParser {
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

            productImage = document.select(".product .wp-post-image").attr("abs:src");
            regularPrice = document.select("meta[itemprop=price]").attr("content");
            description = document.select("#tab-description").text().replace("Product Description", "");
            category = webPageEntity.getCategory().split(",");

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    @Override
    String getSite() {
        return "internationalshootingsupplies.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
