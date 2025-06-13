package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class EllwoodeppsRawProductParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsRawProductParser.class);
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("firearm", "firearm");
        mapping.put("Ammo", "ammo");
        mapping.put("Archery Accessories", "misc");
        mapping.put("Barrels", "misc");
        mapping.put("Bayonets", "misc");
        mapping.put("Binoculars", "misc");
        mapping.put("Bipods", "optic");
        mapping.put("Books, Manuals and Videos", "misc");
        mapping.put("Chokes", "misc");
        mapping.put("Firearm Cleaning &Tools ", "misc");
        mapping.put("Grips", "misc");
        mapping.put("Gun Cases", "misc");
        mapping.put("Holsters", "misc");
        mapping.put("Knives", "misc");
        mapping.put("Magazines", "misc");
        mapping.put("Militaria", "misc");
        mapping.put("Misc Gun Parts", "misc");
        mapping.put("Misc Hunting Accessories", "misc");
        mapping.put("Reloading", "reload");
        mapping.put("Scopes & Mounts", "optic");
        mapping.put("Shooting Accessories", "misc");
        mapping.put("Stocks", "misc");
    }

    public EllwoodeppsRawProductParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            LOGGER.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return price;
        }
    }

    @Override
    public Flux<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            ProductEntity product;
            String productName = null;
            String url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

            if (!document.select(".firearm-links-sold").isEmpty()) {
                return Flux.empty();
            }

            productName = document.select(".product-name span").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();
            if (!document.select(".old-price").isEmpty()) {
                specialPrice = parsePrice(webPageEntity, document.select(".special-price .price").text());
                regularPrice = parsePrice(webPageEntity, document.select(".old-price .price").text());
            } else {
                regularPrice = parsePrice(webPageEntity, document.select(".price").text());
            }
            Iterator<Element> labels = document.select("th.label").iterator();
            Iterator<Element> values = document.select("td.data").iterator();

            while (labels.hasNext()) {
                String specName = labels.next().text();
                String specValue = values.next().text();
                if (!specValue.isEmpty()) {
                    attr.put(specName, specValue);
                }
            }
            category = getNormalizedCategories(webPageEntity);

            product = ProductEntity.legacyCreate(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Flux.fromIterable(result)
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
        return "ellwoodepps.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
