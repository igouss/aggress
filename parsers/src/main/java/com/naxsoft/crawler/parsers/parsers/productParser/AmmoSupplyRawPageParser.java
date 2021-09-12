package com.naxsoft.crawler.parsers.parsers.productParser;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.common.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AmmoSupplyRawPageParser extends AbstractRawPageParser {
    private static final Pattern priceParser = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("ammo", "ammo");
    }

    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = priceParser.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            log.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return price;
        }
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
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
            productName = document.select(".p-name").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (document.select(".ct-counts").text().equals("Out Of Stock")) {
                log.info("Ignoring {}", webPageEntity.getUrl());
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
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory().toUpperCase();
        String s = mapping.get(category);
        if (null != s) {
            return s.split(",");
        }
        log.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "ammosupply.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }
}
