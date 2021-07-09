package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
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
class WestrifleProductRawParser extends AbstractRawPageParser {
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("RUSSIAN SKS", "firearm");
        mapping.put("MOSIN NAGANT 91/30", "firearm");
        mapping.put("SHOTGUNS", "firearm");
        mapping.put("AMMUNITIONS", "ammo");
        mapping.put("MOSIN NAGANT PARTS", "misc");
        mapping.put("SKS PARTS", "misc");
        mapping.put("RIFLE SCOPES", "optic");
        mapping.put("ACCESSORIES", "misc");
        mapping.put("BIPODS", "misc");
        mapping.put("MAGAZINES", "misc");
        mapping.put("MOUNTS", "misc");
        mapping.put("MUZZLEBRAKES", "misc");
        mapping.put("STOCKS", "misc");
        mapping.put("COLLECTIBLE MOSIN NAGANT 91/30", "firearm");
        mapping.put("ROCK SOLID MOUNTS", "misc");
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
            log.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return price;
        }
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
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

            if (document.select("#productDetailsList > li").text().equals("0 Units in Stock")) {
                return result;
            }

            productName = document.select("#productName").text();
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            url = webPageEntity.getUrl();
            productImage = document.select("#productMainImage a > img").attr("abs:src");
            description = document.select("#productDescription").text();
            category = getNormalizedCategories(webPageEntity);
            regularPrice = parsePrice(webPageEntity, document.select("#productPrices").text());

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        if (mapping.containsKey(webPageEntity.getCategory())) {
            return mapping.get(webPageEntity.getCategory()).split(",");
        }
        log.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "westrifle.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
