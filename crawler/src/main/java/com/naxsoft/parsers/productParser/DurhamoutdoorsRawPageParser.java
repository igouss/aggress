package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DurhamoutdoorsRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DurhamoutdoorsRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("accessories", "misc");
        mapping.put("Ammo and reloading", "ammo");
        mapping.put("Glock", "firearm");
        mapping.put("GSG1911", "firearm");
        mapping.put("HATSAN", "firearm");
        mapping.put("HK", "firearm");
        mapping.put("On Sale!", "firearm,optic,misc");
        mapping.put("Optics", "optic");
        mapping.put("Outdoor radios", "misc");
        mapping.put("Pistols", "firearm");
        mapping.put("Rifles", "firearm");
        mapping.put("Shotgun", "firearm");
    }

    public DurhamoutdoorsRawPageParser(MetricRegistry metricRegistry) {
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

    /**
     * @param webPageEntity
     * @return
     * @throws Exception
     */
    @Override
    public Collection<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            ProductEntity product;
            String productName;
            String url;
            String regularPrice;
            String specialPrice = null;
            String productImage;
            String description;
            Map<String, String> attr = new HashMap<>();
            String[] category;

            url = webPageEntity.getUrl();

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            productName = document.select(".content .title").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select("#add-to-cart").attr("value").equalsIgnoreCase("Sold Out")) {
                return result;
            }

            productImage = document.select(".images img").attr("abs:src");
            regularPrice = parsePrice(webPageEntity, document.select(".content .price").text());
            description = document.select(".content .description").text();
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
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
        return "durhamoutdoors.ca";
    }
}
