package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
class TradeexCanadaRawProductPageParser extends AbstractRawPageParser {
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

            productName = document.select("h1.title").text();
            if (productName.toUpperCase().contains("OUT OF STOCK") || productName.contains("Donation to the CSSA") || productName.contains("SOLD")) {
                return result;
            }
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();

            productImage = document.select(".main-product-image img").attr("abs:src");
            description = document.select(".product-body").text();
            regularPrice = parsePrice(webPageEntity, document.select("#price-group .product span").text());
            category = getNormalizedCategories(webPageEntity);
            Iterator<Element> labels = document.select(".product-additional .field-label").iterator();
            Iterator<Element> values = document.select(".product-additional .field-items").iterator();

            while (labels.hasNext()) {
                String specName = labels.next().text().replace(":", "").trim();
                String specValue = values.next().text();
                attr.put(specName, specValue);
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
        return "tradeexcanada.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
