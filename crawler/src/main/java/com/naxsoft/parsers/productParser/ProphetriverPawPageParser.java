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
public class ProphetriverPawPageParser  extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleProductRawParser.class);
    private static Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Rifles", "firearm");
        mapping.put("Shotguns", "firearm");
        mapping.put("Handguns", "firearm");
        mapping.put("Ammunition", "ammo");
        mapping.put("Reloading Equipment", "reload");
        mapping.put("Reloading Components", "reload");
        mapping.put("Rifle Scopes", "optic");
        mapping.put("Optics Accessories", "optic");
        mapping.put("Other Optics", "optic");
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());


        String productName = document.select(".BlockContent > h2").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());
        webPageEntity.setCategory(document.select("#ProductBreadcrumb > ul > li:nth-child(2) > a").text());

        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            jsonBuilder.field("productName", productName);
            String img = document.select(".ProductThumbImage img").attr("abs:src");
            if (!img.contains("DefaultProductImageCustom.jpg")) {
                jsonBuilder.field("productImage", img);
            }
            jsonBuilder.field("description", document.select(".ProductDescriptionContainer").text());
            jsonBuilder.field("regularPrice", parsePrice(document.select(".ProductPrice").text()));
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
        if (mapping.containsKey(webPageEntity.getCategory())) {
            return mapping.get(webPageEntity.getCategory()).split(",");
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
        return webPage.getUrl().startsWith("http://store.prophetriver.com/") && webPage.getType().equals("productPageRaw");
    }
}
