package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.reactivex.Flowable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class CanadaAmmoRawPageParser extends AbstractRawPageParser implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadaAmmoRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern priceMatcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("Ammunition", "ammo");
        mapping.put("Firearms", "firearm");
        mapping.put("Accessories", "misc");
        mapping.put("Equipment", "misc");
        mapping.put("Range Accessories", "misc");
        mapping.put("Optics", "optic");
        mapping.put("Bargain Centre", "firearm,misc,ammo");
    }

    public CanadaAmmoRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = priceMatcher.matcher(price);
        if (matcher.find()) {
            try {
                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                return Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            LOGGER.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return price;
        }
    }

    @Override
    public Flowable<ProductEntity> parse(WebPageEntity webPageEntity) {
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

            String webPageEntityUrl = webPageEntity.getUrl();
            if (webPageEntityUrl.contains("&zenid=")) {
                int zenIndex = webPageEntityUrl.indexOf("&zenid=");
                webPageEntityUrl = webPageEntityUrl.substring(0, zenIndex);
            }
            url = webPageEntityUrl;
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntityUrl);
            if (document.select(".product-details__add").isEmpty()) {
                return Flowable.empty();
            } else if (document.select(".product-details__warranty-text").text().contains("sold out")) {
                return Flowable.empty();
            }
            productName = document.select(".product-details__title .product__name").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntityUrl);
            attr.put("manufacturer", document.select(".product-details__title .product__manufacturer").text());
            productImage = document.select("img[itemprop=image]").attr("srcset");
            String regularPriceStrike = document.select("div.product-details__main .product__price del").text();
            if (regularPriceStrike.isEmpty()) {
                regularPrice = parsePrice(webPageEntity, document.select("div.product-details__main .product__price").text());
            } else {
                regularPrice = parsePrice(webPageEntity, regularPriceStrike);
                specialPrice = parsePrice(webPageEntity, document.select("div.product-details__main .product__price").first().child(0).text());
            }

            description = document.select("div.product-details__meta-wrap > div > div > div:nth-child(1) > section span").text();
            category = getNormalizedCategories(webPageEntity);

            Iterator<Element> labels = document.select(".product-details__spec-label").iterator();
            Iterator<Element> values = document.select(".product-details__spec-value").iterator();

            while (labels.hasNext()) {
                String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_'));
                String specValue = values.next().text();
                attr.put(specName, specValue);
            }

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Flowable.fromIterable(result)
                .doOnNext(e -> parseResultCounter.inc());
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
        return "canadaammo.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
