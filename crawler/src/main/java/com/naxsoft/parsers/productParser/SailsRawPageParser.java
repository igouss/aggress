package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SailsRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SailsRawPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    public SailsRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

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

            productName = document.select(".product-shop .brand").text() + " " + document.select(".product-shop .product-name span").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();

            productImage = document.select(".product-image-gallery > img#image-main").attr("abs:src");
            description = document.select(".product-shop .short-description").text() + " " + document.select("div[data-component=product-description-region]").text();
            category = getNormalizedCategories(webPageEntity);
            if (!document.select(".product-shop .special-price").isEmpty()) {
                regularPrice = parsePrice(webPageEntity, document.select(".product-shop .old-price .price").text());
                specialPrice = parsePrice(webPageEntity, document.select(".product-shop .special-price .price").text());
            } else {
                regularPrice = parsePrice(webPageEntity, document.select(".product-shop .regular-price .price").text());
            }

            Iterator<Element> labels = document.select(".spec-row .label").iterator();
            Iterator<Element> values = document.select(".spec-row .data").iterator();

            while (labels.hasNext()) {
                String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_').replace(":", "").trim());
                String specValue = values.next().text();
                attr.put(specName, specValue);
            }

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (null != category) {
            return category.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "sail.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}