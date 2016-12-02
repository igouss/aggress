package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class AlflahertysRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlflahertysRawPageParser.class);
    private static final Pattern priceParser = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("FIREARMS", "firearm")
            .put("HANDGUNS", "firearm")
            .put("RESTRICTED RIFLES", "firearm")
            .put("RIFLES", "firearm")
            .put("SHOTGUNS", "firearm")
            .put("BLACK POWDER", "firearm")
            .put("HANDGUN AMMUNITION", "ammo")
            .put("BULK RIFLE AMMO", "ammo")
            .put("RIFLE AMMO", "ammo")
            .put("RIMFIRE AMMUNTION", "ammo")
            .put("SHOTGUN AMMO", "ammo")
            .put("RELOADING", "reload")
            .put("SCOPES", "optic")
            .put("CLOSE QUARTERS optic & IRON SIGHTS", "optic")
            .put("RANGE FINDERS", "optic")
            .put("SPOTTING SCOPES", "optic")
            .put("BINOCULARS", "optic")
            .put("OPTIC CARE", "optic")
            .put("OPTIC MOUNTS", "optic")
            .put("NIGHT VISION", "optic")
            .put("SIGHTING TOOLS", "optic")
            .put("HANDGUN CASES", "misc")
            .put("SOFT CASES", "misc")
            .put("HARD CASES", "misc")
            .put("RANGE BAGS", "misc")
            .put("AMMUNITION STORAGE", "misc")
            .put("CABINETS & SAFES", "misc")
            .put("SAFE ACCESSORIES", "misc")
            .put("LOCKS", "misc")
            .put("HOLSTERS, MAG POUCHES, & SHELL HOLDERS", "misc")
            .put("LIGHTS & LASERS", "misc")
            .put("RAILS & MOUNTS", "misc")
            .put("UTILITY BAGS & PACKS", "misc")
            .put("TACTICAL TOOLS", "misc")
            .put("AR COMPONENTS", "misc")
            .put("GRIPS", "misc")
            .put("RIFLE PARTS & STOCKS", "misc")
            .put("HANDGUN PARTS", "misc")
            .put("SHOTGUN PARTS & STOCKS", "misc")
            .put("SHOTGUN BARRELS & CHOKES", "misc")
            .put("CONVERSION KITS", "misc")
            .put("FIREARM MAINTENANCE & TOOLS", "misc")
            .put("BIPODS AND SHOOTING RESTS", "misc")
            .put("SLINGS & SWIVELS", "misc")
            .put("EYES & EARS", "misc")
            .put("CLIPS & MAGAZINES", "misc")
            .put("SHOTGUN ACCESSORIES", "misc")
            .put("TARGETS", "misc")
            .put("ACCESSORIES", "misc")
            .put("FIELD DRESSING & TOOLS", "misc")
            .put("GAME CALLS DECOYS & ACCESSORIES", "misc")
            .put("SCENTS, DETERGENTS, & ATTRACTANTS", "misc")
            .put("BLINDS & CAMOUFLAGE", "misc")
            .put("TRAIL CAMERAS", "misc")
            .put("HUNTING CLOTHES", "misc")
            .build();

    public AlflahertysRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = priceParser.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            LOGGER.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return price;
        }
    }

    @Override
    public Collection<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            String productName;
            String url;
            String regularPrice;
            String specialPrice = null;
            String productImage;
            String description;
            Map<String, String> attr = new HashMap<>();
            String[] category;

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            productName = document.select(".product_name").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (!document.select(".product_section .sold_out").text().equals("Sold Out")) {
                ProductEntity product;
                url = webPageEntity.getUrl();
                productImage = document.select("meta[property=og:image]").attr("content");

                if (document.select(".product_section .was_price").text().equals("")) {
                    regularPrice = parsePrice(webPageEntity, document.select(".product_section .current_price").text());
                } else {
                    regularPrice = parsePrice(webPageEntity, document.select(".product_section .was_price").text());
                    specialPrice = parsePrice(webPageEntity, document.select(".product_section .current_price").text());
                }
                description = document.select(".product_section .description").text();
                category = getNormalizedCategories(webPageEntity);

                Iterator<Element> labels = document.select(".meta span:nth-child(1)").iterator();
                Iterator<Element> values = document.select(".meta span:nth-child(2)").iterator();
                while (labels.hasNext()) {
                    String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_').replace(":", "").trim());
                    String specValue = values.next().text();
                    if (!specName.equalsIgnoreCase("category")) {
                        attr.put(specName, specValue);
                    }
                }
                product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
                result.add(product);
            }
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
        String category = webPageEntity.getCategory().toUpperCase();
        String s = mapping.get(category);
        if (null != s) {
            return s.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "alflahertys.com";
    }
}
