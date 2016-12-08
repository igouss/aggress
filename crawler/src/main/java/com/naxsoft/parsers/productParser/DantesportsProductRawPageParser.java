package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class DantesportsProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsProductRawPageParser.class);
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("Shotguns", "firearm")
            .put("Rifles", "firearm")
            .put("Restricted", "firearm")
            .put("Air Rifles", "firearm")
            .put("Prohibited", "firearm")
            .put("CVA", "firearm")
            .put("Thompson/Center", "firearm")
            .put("Bushnell", "optic")
            .put("Burris", "optic")
            .put("Kaps", "optic")
            .put("Leupold", "optic")
            .put("Nightforce", "optic")
            .put("Nikon", "optic")
            .put("Redfield", "optic")
            .put("Swarovski", "optic")
            .put("Tasco", "optic")
            .put("Vortex", "optic")
            .put("Zeiss", "optic")
            .put("Binoculars", "optic")
            .put("Holographic Sights", "optic")
            .put("Laser Sights", "optic")
            .put("Illuminated Dot Sights", "optic")
            .put("Rangefinders", "optic")
            .put("Accessories", "optic")
            .put("Shotshells", "ammo")
            .put("Centerfire", "ammo")
            .put("Rimfire", "ammo")
            .put("Cases", "misc")
            .put("Ruger Firearm Accessories", "misc")
            .put("Gunsmithing Tools", "misc")
            .put("Shooting Rests", "misc")
            .put("Trail Camera", "misc")
            .put("Target Thrower", "misc")
            .put("Magazines", "misc")
            .put("Tactical Accessories", "misc")
            .put("Hearing Protection", "misc")
            .put("Knives", "misc")
            .put("ZEV Technologies", "misc")
            .put("CamPro", "reload")
            .put("Frankford Arsenal", "reload")
            .put("Redding", "reload")
            .put("Hornady", "reload")
            .put("Carbon Express", "misc")
            .build();
    private static final Pattern pricePattern = Pattern.compile("(\\d+|,+)+\\.\\d\\d");

    public DantesportsProductRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    /**
     * @param webPageEntity
     * @return
     * @throws Exception
     */
    @Override
    public Collection<ProductEntity> parse(WebPageEntity webPageEntity) {
        ImmutableSet.Builder<ProductEntity> result = ImmutableSet.builder();
        try {
            ProductEntity product;
            String productName;
            String url;
            String regularPrice = null;
            String specialPrice = null;
            String productImage;
            String description;
            Map<String, String> attr = new HashMap<>();
            String[] category;

            url = webPageEntity.getUrl();

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            productName = document.select(".naitem").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (!document.select(".outofstock").isEmpty()) {
                return result.build();
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
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return result.build();
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
        return "dantesports.com";
    }
}
