package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class ProphetriverPawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleProductRawParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Rifles", "firearm");
        mapping.put("Shotguns", "firearm");
        mapping.put("Handguns", "firearm");
        mapping.put("Ammunition", "ammo");
        mapping.put("Reloading Equipment", "reload");
        mapping.put("Reloading Components", "reload");
        mapping.put("Rifle Scopes", "optic");
        mapping.put("Optics Accessories", "optic");
        mapping.put("Other Optics", "optic");
    }

    public ProphetriverPawPageParser(MetricRegistry metricRegistry) {
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
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
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

            productName = document.select(".BlockContent > h2").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());
            category = getNormalizedCategories(document.select("#ProductBreadcrumb > ul > li:nth-child(2) > a").text());


            url = webPageEntity.getUrl();


            String img = document.select(".ProductThumbImage img").attr("abs:src");
            if (!img.contains("DefaultProductImageCustom.jpg")) {
                productImage = img;
            }
            description = document.select(".ProductDescriptionContainer").text();
            regularPrice = parsePrice(webPageEntity, document.select(".ProductPrice").text());


            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result)
                .doOnNext(e -> parseResultCounter.inc());
    }

    /**
     * @param category
     * @return
     */
    private String[] getNormalizedCategories(String category) {
        if (mapping.containsKey(category)) {
            return mapping.get(category).split(",");
        }
        LOGGER.warn("Unknown category: {}", category);
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "prophetriver.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
