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
public class DantesportsProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsProductRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Shotguns", "firearm");
        mapping.put("Rifles", "firearm");
        mapping.put("Restricted", "firearm");
        mapping.put("Air Rifles", "firearm");

        mapping.put("Shotguns", "firearm");
        mapping.put("Rifles", "firearm");
        mapping.put("Prohibited", "firearm");
        mapping.put("Restricted", "firearm");

        mapping.put("CVA", "firearm");
        mapping.put("Thompson/Center", "firearm");

        mapping.put("Bushnell", "optic");
        mapping.put("Burris", "optic");
        mapping.put("Kaps", "optic");
        mapping.put("Leupold", "optic");
        mapping.put("Nightforce", "optic");
        mapping.put("Nikon", "optic");
        mapping.put("Redfield", "optic");
        mapping.put("Swarovski", "optic");
        mapping.put("Tasco", "optic");
        mapping.put("Vortex", "optic");
        mapping.put("Zeiss", "optic");

        mapping.put("Binoculars", "optic");
        mapping.put("Holographic Sights", "optic");
        mapping.put("Laser Sights", "optic");
        mapping.put("Illuminated Dot Sights", "optic");
        mapping.put("Rangefinders", "optic");
        mapping.put("Accessories", "optic");

        mapping.put("Shotshells", "ammo");
        mapping.put("Centerfire", "ammo");
        mapping.put("Rimfire", "ammo");

        mapping.put("Cases", "misc");
        mapping.put("Ruger Firearm Accessories", "misc");
        mapping.put("Gunsmithing Tools", "misc");
        mapping.put("Shooting Rests", "misc");
        mapping.put("Trail Camera", "misc");
        mapping.put("Target Thrower", "misc");
        mapping.put("Magazines", "misc");
        mapping.put("Tactical Accessories", "misc");
        mapping.put("Hearing Protection", "misc");
        mapping.put("Knives", "misc");
        mapping.put("ZEV Technologies", "misc");

        mapping.put("CamPro", "reload");
        mapping.put("Frankford Arsenal", "reload");
        mapping.put("Redding", "reload");
        mapping.put("Hornady", "reload");

        mapping.put("Carbon Express", "misc");
    }

    /**
     * @param webPageEntity
     * @return
     * @throws Exception
     */
    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            String productName = document.select(".naitem").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (!document.select(".outofstock").isEmpty()) {
                return products;
            }

            jsonBuilder.field("productName", productName);
            jsonBuilder.field("productImage", document.select(".itemImgDiv img.itemDetailImg").attr("abs:src"));

            String priceText = document.select(".itemDetailPrice").text().replace("\\xEF\\xBF\\xBD", " ");
            Matcher matcher = Pattern.compile("(\\d+|,+)+\\.\\d\\d").matcher(priceText);
            if (matcher.find()) {
                jsonBuilder.field("regularPrice", matcher.group().replace(",", ""));
            }
            jsonBuilder.field("description", document.select(".itemDescription").text());
            jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setWebpageId(webPageEntity.getId());
            product.setJson(jsonBuilder.string());
        }
        products.add(product);
        return products;

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

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("productPageRaw");
    }
}
