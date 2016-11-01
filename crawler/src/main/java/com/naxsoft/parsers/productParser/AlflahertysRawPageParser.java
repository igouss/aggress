package com.naxsoft.parsers.productParser;

import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class AlflahertysRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlflahertysRawPageParser.class);
    private static final Pattern priceParser = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("FIREARMS", "firearm");
        mapping.put("HANDGUNS", "firearm");
        mapping.put("RESTRICTED RIFLES", "firearm");
        mapping.put("RIFLES", "firearm");
        mapping.put("SHOTGUNS", "firearm");
        mapping.put("BLACK POWDER", "firearm");

        mapping.put("HANDGUN AMMUNITION", "ammo");
        mapping.put("BULK RIFLE AMMO", "ammo");
        mapping.put("RIFLE AMMO", "ammo");
        mapping.put("RIMFIRE AMMUNTION", "ammo");
        mapping.put("SHOTGUN AMMO", "ammo");
        mapping.put("RELOADING", "reload");

        mapping.put("SCOPES", "optic");
        mapping.put("CLOSE QUARTERS optic & IRON SIGHTS", "optic");
        mapping.put("RANGE FINDERS", "optic");
        mapping.put("SPOTTING SCOPES", "optic");
        mapping.put("BINOCULARS", "optic");
        mapping.put("OPTIC CARE", "optic");
        mapping.put("OPTIC MOUNTS", "optic");
        mapping.put("NIGHT VISION", "optic");
        mapping.put("SIGHTING TOOLS", "optic");

        mapping.put("HANDGUN CASES", "misc");
        mapping.put("SOFT CASES", "misc");
        mapping.put("HARD CASES", "misc");
        mapping.put("RANGE BAGS", "misc");
        mapping.put("AMMUNITION STORAGE", "misc");
        mapping.put("CABINETS & SAFES", "misc");
        mapping.put("SAFE ACCESSORIES", "misc");
        mapping.put("LOCKS", "misc");

        mapping.put("HOLSTERS, MAG POUCHES, & SHELL HOLDERS", "misc");
        mapping.put("LIGHTS & LASERS", "misc");
        mapping.put("RAILS & MOUNTS", "misc");
        mapping.put("UTILITY BAGS & PACKS", "misc");
        mapping.put("TACTICAL TOOLS", "misc");

        mapping.put("AR COMPONENTS", "misc");
        mapping.put("GRIPS", "misc");
        mapping.put("RIFLE PARTS & STOCKS", "misc");
        mapping.put("HANDGUN PARTS", "misc");
        mapping.put("SHOTGUN PARTS & STOCKS", "misc");
        mapping.put("SHOTGUN BARRELS & CHOKES", "misc");
        mapping.put("CONVERSION KITS", "misc");

        mapping.put("FIREARM MAINTENANCE & TOOLS", "misc");
        mapping.put("BIPODS AND SHOOTING RESTS", "misc");
        mapping.put("SLINGS & SWIVELS", "misc");
        mapping.put("EYES & EARS", "misc");
        mapping.put("CLIPS & MAGAZINES", "misc");
        mapping.put("SHOTGUN ACCESSORIES", "misc");
        mapping.put("TARGETS", "misc");

        mapping.put("ACCESSORIES", "misc");
        mapping.put("FIELD DRESSING & TOOLS", "misc");
        mapping.put("GAME CALLS DECOYS & ACCESSORIES", "misc");
        mapping.put("SCENTS, DETERGENTS, & ATTRACTANTS", "misc");
        mapping.put("BLINDS & CAMOUFLAGE", "misc");
        mapping.put("TRAIL CAMERAS", "misc");
        mapping.put("HUNTING CLOTHES", "misc");
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
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            String productName = null;
            String url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

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
        return Observable.from(result);
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

    @Override
    String getType() {
        return "productPageRaw";
    }
}
