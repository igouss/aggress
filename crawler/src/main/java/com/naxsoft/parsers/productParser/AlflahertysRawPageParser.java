package com.naxsoft.parsers.productParser;

import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class AlflahertysRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlflahertysRawPageParser.class);
    private static Map<String, String> mapping = new HashMap<>();

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
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
        String productName = document.select(".product_name").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        if (!document.select(".product_section .sold_out").text().equals("Sold Out")) {
            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select("meta[property=og:image]").attr("content"));

                if (document.select(".product_section .was_price").text().equals("")) {
                    jsonBuilder.field("regularPrice", parsePrice(document.select(".product_section .current_price").text()));
                } else {
                    jsonBuilder.field("regularPrice", parsePrice(document.select(".product_section .was_price").text()));
                    jsonBuilder.field("specialPrice", parsePrice(document.select(".product_section-secondary .price-current_price").text()));
                }
                jsonBuilder.field("description", document.select(".product_section .description").text());
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));

                Iterator<Element> labels = document.select(".meta span:nth-child(1)").iterator();
                Iterator<Element> values = document.select(".meta span:nth-child(2)").iterator();
                while (labels.hasNext()) {
                    String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_').replace(":", "").trim());
                    String specValue = values.next().text();
                    if (!specName.equalsIgnoreCase("category")) {
                        jsonBuilder.field(specName, specValue);
                    }
                }
                jsonBuilder.endObject();
                product.setUrl(document.location());
                product.setJson(jsonBuilder.string());
            }
            product.setWebpageId(webPageEntity.getId());
            result.add(product);
        }

        return result;
    }

    /**
     *
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
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("productPageRaw");
    }
}
