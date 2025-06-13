package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class GunshopRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GunshopRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("/product-category/firearms/", "firearm");
        mapping.put("/product-category/ammunition/", "ammo");
        mapping.put("/product-category/optics/", "optic");
        mapping.put("/product-category/reloading-components/", "reload");
    }

    public GunshopRawPageParser(MetricRegistry metricRegistry) {
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

            Elements productNameEl = document.select("h1.entry-title");
            if (!productNameEl.isEmpty()) {
                productName = productNameEl.first().text();
                LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());
            } else {
                LOGGER.warn("Unable to find product name {}", webPageEntity);
                return Flux.empty();
            }

            if (!document.select(".entry-summary .out-of-stock").isEmpty()) {
                LOGGER.info("Product {} is out of stock. {}", productName, webPageEntity.getUrl());
                return Flux.empty();
            }

            url = webPageEntity.getUrl();
            productImage = document.select(".wp-post-image").attr("src");
            specialPrice = document.select(".entry-summary .price ins span").text();
            if (specialPrice.isEmpty()) {
                regularPrice = parsePrice(webPageEntity, document.select(".entry-summary .amount").text());
            } else {
                specialPrice = parsePrice(webPageEntity, specialPrice);
                regularPrice = parsePrice(webPageEntity, document.select(".entry-summary del .amount").text());
            }

            description = document.select("#tab-description").text();
            for (Element next : document.select("product_meta span")) {
                String name = next.data();
                if (!name.equalsIgnoreCase("categories")) {
                    Elements values = next.select("a");
                    attr.put(name, values.text());
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
        for (String urlPattern : mapping.keySet()) {
            if (webPageEntity.getUrl().contains(urlPattern)) {
                return mapping.get(urlPattern).split(",");
            }
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "gun-shop.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
