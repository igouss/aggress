package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.reactivex.Flowable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AmmoSupplyRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmmoSupplyRawPageParser.class);
    private static final Pattern priceParser = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("ammo", "ammo");
    }

    public AmmoSupplyRawPageParser(MetricRegistry metricRegistry) {
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
    public Flowable<ProductEntity> parse(WebPageEntity webPageEntity) {
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
            productName = document.select(".p-name").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select(".ct-counts").text().equals("Out Of Stock")) {
                LOGGER.info("Ignoring {}", webPageEntity.getUrl());
            } else {
                ProductEntity product;
                url = webPageEntity.getUrl();
                productImage = document.select(".item-img img").attr("src").trim();
                regularPrice = parsePrice(webPageEntity, document.select(".price").text());
                description = document.select("#tab-description").text();
                category = getNormalizedCategories(webPageEntity);

                attr.put("rounds", document.select(".p-rounds").first().text());

                product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
                result.add(product);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Flowable.fromIterable(result);
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
        return "ammosupply.ca";
    }
}
