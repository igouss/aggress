package com.naxsoft.parsers.productParser;

import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BullseyelondonProductRawPageParser extends AbstractRawPageParser implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BullseyelondonProductRawPageParser.class);

    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern freeShippingPattern = Pattern.compile("\\w+|\\s+");
    private static final Pattern unitsAvailablePattern = Pattern.compile("\\d+");
    private static final Pattern priceMatcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

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

    private static String getFreeShipping(Document document) {
        if (!document.select(".freeShip").isEmpty()) {
            String raw = document.select(".freeShip").first().text();
            Matcher matcher = freeShippingPattern.matcher(raw);
            return matcher.find() ? "true" : "false";
        } else {
            return "false";
        }
    }

    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = priceMatcher.matcher(price);
        String result;
        if (matcher.find()) {
            try {
                result = NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                result = Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            LOGGER.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            result = price;
        }
        return result;
    }

    private static String getRegularPrice(WebPageEntity webPageEntity, Document document) {
        String raw = document.select(".regular-price").text().trim();
        if (raw.isEmpty()) {
            raw = document.select(".old-price .price").text().trim();
        }
        return parsePrice(webPageEntity, raw);
    }

    private static String getSpecialPrice(WebPageEntity webPageEntity, Document document) {
        String raw = document.select(".special-price .price").text().trim();
        String price = "";
        if (!raw.isEmpty()) {
            price = parsePrice(webPageEntity, raw);
        }
        return price;
    }

    private static String getUnitsAvailable(Document document) {
        Elements priceBox = document.select(".price-box");
        String result = "";
        if (!priceBox.isEmpty()) {
            String raw = priceBox.first().nextElementSibling().text().trim();

            Matcher matcher = unitsAvailablePattern.matcher(raw);
            if (matcher.find()) {
                result = matcher.group(0);
            } else {
                result = raw;
            }
        }
        return result;
    }

    @Override
    public Iterable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            ProductEntity product;
            String productName;
            URL url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            Elements productNameEl = document.select(".product-name h1");
            if (!productNameEl.isEmpty()) {
                productName = productNameEl.first().text().trim();
                LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());
            } else {
                LOGGER.warn("unable to find product name {}", webPageEntity);
                return List.of();
            }

            url = webPageEntity.getUrl();

            category = getNormalizedCategories(webPageEntity);
            productImage = document.select("#product_addtocart_form > div.product-img-box > p > a > img").attr("src").trim();
            regularPrice = getRegularPrice(webPageEntity, document);
            specialPrice = getSpecialPrice(webPageEntity, document);
            attr.put("freeShipping", BullseyelondonProductRawPageParser.getFreeShipping(document));
            attr.put("unitsAvailable", BullseyelondonProductRawPageParser.getUnitsAvailable(document));
            description = document.select(".short-description").text().trim();

            Elements table = document.select("#product_tabs_additional_contents");

            for (Element row : table.select("tr")) {
                String th = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, row.select("th").text().replace(' ', '-'));
                String td = row.select("td").text();
                if (!th.equalsIgnoreCase("category")) {
                    attr.put(th, td);
                }
            }
            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String s = mapping.get(webPageEntity.getCategory());
        if (null != s) {
            return s.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[] { "misc" };
    }

    @Override
    String getSite() {
        return "bullseyelondon.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }
}
