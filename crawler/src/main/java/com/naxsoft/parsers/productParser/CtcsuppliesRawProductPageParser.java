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
public class CtcsuppliesRawProductPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CtcsuppliesRawProductPageParser.class);

    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Optics / Sights / Flashlights", "optic");
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
        String productName = document.select(".product-single h1").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        if (document.select("#AddToCartText").text().equalsIgnoreCase("Sold Out")) {
            return result;
        }

        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            jsonBuilder.field("productName", productName);
            jsonBuilder.field("productImage", document.select("#ProductPhotoImg").attr("src"));
            jsonBuilder.field("manufacturer", document.select(".product-single h3").text());
            jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
            jsonBuilder.field("regularPrice", parsePrice(document.select("#ProductPrice").text()));
            jsonBuilder.field("description", document.select(".product-description p").text());
            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setJson(jsonBuilder.string());
        }
        product.setWebpageId(webPageEntity.getId());
        result.add(product);

        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String s = mapping.get(webPageEntity.getCategory());
        if (null != s) {
            String[] result = s.split(",");
            return result;
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
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
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://ctcsupplies.ca/") && webPage.getType().equals("productPageRaw");
    }
}
