package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class QuestarRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestarRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Firearms-Handguns", "firearm");
        mapping.put("Firearms-Long Guns", "firearm");
        mapping.put("Sights/Lasers/Scopes", "optic");
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            LOGGER.error("failed to parse price {}", price);
            return price;
        }
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws ProductParseException {
        try {
            HashSet<ProductEntity> result = new HashSet<>();
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            String productName = document.select("#main > table > tbody > tr:nth-child(1) > td > table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(1)").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select("a[rel=lightbox] img").attr("abs:src"));
                jsonBuilder.field("description", document.select("#main > table > tbody > tr:nth-child(2) > td").text());
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
                jsonBuilder.field("regularPrice", parsePrice(document.select("#main td > span.price").text()));
                jsonBuilder.endObject();
                product.setUrl(webPageEntity.getUrl());
                product.setJson(jsonBuilder.string());
            }
            product.setWebpageId(webPageEntity.getId());
            result.add(product);
            return result;
        } catch (Exception e) {
            throw new ProductParseException(e);
        }
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
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shopquestar.com/") && webPage.getType().equals("productPageRaw");
    }
}