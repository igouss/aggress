package com.naxsoft.crawler.parsers.parsers.productParser;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.common.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
class DantesportsProductRawPageParser extends AbstractRawPageParser {
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("(\\d+|,+)+\\.\\d\\d");

    static {
        mapping.put("Shotguns", "firearm");
        mapping.put("Rifles", "firearm");
        mapping.put("Restricted", "firearm");
        mapping.put("Air Rifles", "firearm");

        mapping.put("Prohibited", "firearm");

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

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            ProductEntity product;
            String productName = null;
            String url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

            url = webPageEntity.getUrl();

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            productName = document.select(".naitem").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (!document.select(".outofstock").isEmpty()) {
                return result;
            }

            productImage = document.select(".itemImgDiv img.itemDetailImg").attr("abs:src");
            String priceText = document.select(".itemDetailPrice").text().replace("\\xEF\\xBF\\xBD", " ");
            Matcher matcher = pricePattern.matcher(priceText);
            if (matcher.find()) {
                regularPrice = matcher.group().replace(",", "");
            }
            description = document.select(".itemDescription").text();
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String s = mapping.get(webPageEntity.getCategory());
        if (null != s) {
            return s.split(",");
        }
        log.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};

    }

    @Override
    String getSite() {
        return "dantesports.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
