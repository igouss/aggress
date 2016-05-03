//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BullseyelondonProductRawPageParser extends AbstractRawPageParser implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BullseyelondonProductRawPageParser.class);

    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Pistols", "firearm");
        mapping.put("Long Guns", "firearm");
        mapping.put("Ammunition", "ammo");
        mapping.put("Surplus", "ammo,firearm");
        mapping.put("Magazines", "misc");
        mapping.put("Storage", "misc");
        mapping.put("Reloading", "reload");
        mapping.put("Accessories", "misc");
        mapping.put("Optics", "optic");
    }

    /**
     * @param document
     * @return
     */
    private static String getFreeShipping(Document document) {
        String raw = document.select(".freeShip").first().text();
        Matcher matcher = Pattern.compile("\\w+|\\s+").matcher(raw);
        return matcher.find() ? "true" : "false";
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        String result;
        if (matcher.find()) {
            try {
                result = NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                result = Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            LOGGER.error("failed to parse price {}", price);
            result = price;
        }
        return result;
    }

    /**
     * @param document
     * @return
     */
    private static String getRegularPrice(Document document) {
        String raw = document.select(".regular-price").text().trim();
        if (raw.isEmpty()) {
            raw = document.select(".old-price .price").text().trim();
        }
        return parsePrice(raw);
    }

    /**
     * @param document
     * @return
     */
    private static String getSpecialPrice(Document document) {
        String raw = document.select(".special-price .price").text().trim();
        return parsePrice(raw);
    }

    /**
     * @param document
     * @return
     */
    private static String getUnitsAvailable(Document document) {
        String raw = document.select(".price-box").first().nextElementSibling().text().trim();
        String result;
        Matcher matcher = Pattern.compile("\\d+").matcher(raw);
        if (matcher.find()) {
            result = matcher.group(0);
        } else {
            result = raw;
        }
        return result;
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            String productName = document.select(".product-name h1").first().text().trim();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            document.select(".product-name h1").first().children().remove();
            jsonBuilder.field("productName", productName);
            jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
            jsonBuilder.field("productImage", document.select("#product_addtocart_form > div.product-img-box > p > a > img").attr("src").trim());
            jsonBuilder.field("regularPrice", getRegularPrice(document));
            jsonBuilder.field("specialPrice", getSpecialPrice(document));

            try {
                jsonBuilder.field("freeShipping", BullseyelondonProductRawPageParser.getFreeShipping(document));
            } catch (Exception ignored) {
            }

            jsonBuilder.field("unitsAvailable", BullseyelondonProductRawPageParser.getUnitsAvailable(document));
            jsonBuilder.field("description", document.select(".short-description").text().trim());

            Elements table = document.select("#product_tabs_additional_contents");

            for (Element row : table.select("tr")) {
                String th = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, row.select("th").text().replace(' ', '-'));
                String td = row.select("td").text();
                if (!th.equalsIgnoreCase("category")) {
                    jsonBuilder.field(th, td);
                }
            }

            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setWebpageId(webPageEntity.getId());
            product.setJson(jsonBuilder.string());
        }
        products.add(product);
        return products;
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
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("productPageRaw");
    }
}
