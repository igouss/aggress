package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
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
class CorwinArmsProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorwinArmsProductRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("New Arrivals", "misc");
        mapping.put("Firearms", "firearm");
        mapping.put("Firearm Accessories", "optic,misc");
        mapping.put("Magazines", "misc");
        mapping.put("Knives", "misc");
        mapping.put("Flashlights", "misc");
        mapping.put("Axes", "misc");
        mapping.put("Bayonets", "misc");
        mapping.put("Swords", "misc");
        mapping.put("Optics", "optic");
    }

    public CorwinArmsProductRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
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

            productName = document.select("#maincol h1").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();
            String img = document.select("div.field-type-image img").attr("abs:src");
            if (img.isEmpty()) {
                img = document.select("div.field-name-field-product-mag-image > div > div > img").attr("abs:src");
            }
            productImage = img;
            regularPrice = parsePrice(webPageEntity, document.select(".field-name-commerce-price").text());
            description = document.select("div.field-type-text-with-summary > div > div").text().replace("\u0160", "\n");
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result)
                .doOnNext(e -> parseResultCounter.inc());
    }

    /**
     * @param webPageEntity
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        if (mapping.containsKey(webPageEntity.getCategory())) {
            return mapping.get(webPageEntity.getCategory()).split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "corwin-arms.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
