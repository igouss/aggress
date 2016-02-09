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
public class HicalRawProductParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalRawProductParser.class);

    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Firearms", "misc");
        mapping.put("Handguns", "misc");
        mapping.put("Rifles - Restricted", "misc");
        mapping.put("Rifles- Non restricted", "misc");
        mapping.put("Rimfire", "misc");
        mapping.put("Shotguns", "misc");
        mapping.put("Used Firearms", "misc");

        mapping.put("Sights & Optics", "optic");
        mapping.put("Binoculars & Spotting Scopes", "optic");
        mapping.put("Optic Accessories", "optic");
        mapping.put("Red/ green dot sights", "optic");
        mapping.put("Scope Rings & Bases", "optic");
        mapping.put("Scopes", "optic");
        mapping.put("Sights", "optic");
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

        String productName = document.select("h2[itemprop='name']").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            jsonBuilder.field("productName", productName);

            String regularPrice = document.select("#ProductDetails div.DetailRow.PriceRow > div.Value > em").text();
            String specialPrice = document.select("#ProductDetails div.Value > strike").text();
            if ("".equals(specialPrice)) {
                jsonBuilder.field("regularPrice", parsePrice(regularPrice));
            } else {
                jsonBuilder.field("specialPrice", parsePrice(specialPrice));
                jsonBuilder.field("regularPrice", parsePrice(regularPrice));
            }
            jsonBuilder.field("productImage", document.select("#ProductDetails .ProductThumbImage img").attr("src"));
            jsonBuilder.field("description", document.select("#ProductDescription").text().trim());


            jsonBuilder.field("category", getNormalizedCategories(webPageEntity));

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
            return s.split(",");
        } else {
            LOGGER.error("Invalid category: " + webPageEntity);
            return new String[]{"misc"};
        }
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
            return price;
        }
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("productPageRaw");
    }
}
