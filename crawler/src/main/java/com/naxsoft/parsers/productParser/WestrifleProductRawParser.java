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
class WestrifleProductRawParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleProductRawParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("RUSSIAN SKS", "firearm");
        mapping.put("MOSIN NAGANT 91/30", "firearm");
        mapping.put("SHOTGUNS", "firearm");
        mapping.put("AMMUNITIONS", "ammo");
        mapping.put("MOSIN NAGANT PARTS", "misc");
        mapping.put("SKS PARTS", "misc");
        mapping.put("RIFLE SCOPES", "optic");
        mapping.put("ACCESSORIES", "misc");
        mapping.put("BIPODS", "misc");
        mapping.put("MAGAZINES", "misc");
        mapping.put("MOUNTS", "misc");
        mapping.put("MUZZLEBRAKES", "misc");
        mapping.put("STOCKS", "misc");
        mapping.put("COLLECTIBLE MOSIN NAGANT 91/30", "firearm");
        mapping.put("ROCK SOLID MOUNTS", "misc");
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = pricePattern.matcher(price);
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

            if (document.select("#productDetailsList > li").text().equals("0 Units in Stock")) {
                return result;
            }

            String productName = document.select("#productName").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select("#productMainImage a > img").attr("abs:src"));
                jsonBuilder.field("description", document.select("#productDescription").text());
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
                jsonBuilder.field("regularPrice", parsePrice(document.select("#productPrices").text()));
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
        if (mapping.containsKey(webPageEntity.getCategory())) {
            return mapping.get(webPageEntity.getCategory()).split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("westrifle.com") && webPage.getType().equals("productPageRaw");
    }
}
