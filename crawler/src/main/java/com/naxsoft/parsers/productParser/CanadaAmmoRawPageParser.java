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
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CanadaAmmoRawPageParser extends AbstractRawPageParser implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadaAmmoRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Ammunition", "ammo");
        mapping.put("Firearms", "firearm");
        mapping.put("Accessories", "misc");
        mapping.put("Equipment", "misc");
        mapping.put("Range Accessories", "misc");
        mapping.put("Optics", "optic");
        mapping.put("Bargain Centre", "firearm,misc,ammo");
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            String webPageEntityUrl = webPageEntity.getUrl();
            if (webPageEntityUrl.contains("&zenid=")) {
                int zenIndex = webPageEntityUrl.indexOf("&zenid=");
                webPageEntityUrl = webPageEntityUrl.substring(0, zenIndex);
            }
            jsonBuilder.field("url", webPageEntityUrl);
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntityUrl);

            if (document.select(".product-details__add").isEmpty()) {
                return products;
            }


            String productName = document.select(".product-details__title .product__name").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntityUrl);

            jsonBuilder.field("productName", productName);
            jsonBuilder.field("manufacturer", document.select(".product-details__title .product__manufacturer").text());
            jsonBuilder.field("productImage", document.select("img[itemprop=image]").attr("srcset"));
            String regularPriceStrike = document.select("div.product-details__main .product__price del").text();
            if ("".equals(regularPriceStrike)) {
                jsonBuilder.field("regularPrice", parsePrice(document.select("div.product-details__main .product__price").text()));
            } else {
                jsonBuilder.field("regularPrice", parsePrice(regularPriceStrike));
                jsonBuilder.field("specialPrice", parsePrice(document.select("div.product-details__main .product__price").first().child(0).text()));
            }

            jsonBuilder.field("description", document.select("div.product-details__meta-wrap > div > div > div:nth-child(1) > section span").text());
            jsonBuilder.field("category", getNormalizedCategories(webPageEntity));

            Iterator<Element> labels = document.select(".product-details__spec-label").iterator();
            Iterator<Element> values = document.select(".product-details__spec-value").iterator();

            while (labels.hasNext()) {
                String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_'));
                String specValue = values.next().text();
                jsonBuilder.field(specName, specValue);
            }
            jsonBuilder.endObject();
            product.setUrl(webPageEntityUrl);
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

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            try {
                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                return Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            return price;
        }
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("productPageRaw");
    }
}
