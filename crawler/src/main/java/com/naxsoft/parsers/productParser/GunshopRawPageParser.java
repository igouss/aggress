package com.naxsoft.parsers.productParser;

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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class GunshopRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GunshopRawPageParser.class);
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("/product-category/firearms/", "firearm");
        mapping.put("/product-category/ammunition/", "ammo");
        mapping.put("/product-category/optics/", "optic");
        mapping.put("/product-category/reloading-components/", "reload");
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            return price;
        }
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());


        String productName = document.select("#page-heading  h1").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

            jsonBuilder.field("productName", productName);
            jsonBuilder.field("productImage", document.select(".wp-post-image").attr("src"));

            String specialPrice = document.select(".price ins span").text();
            if ("".equals(specialPrice)) {
                jsonBuilder.field("regularPrice", parsePrice(document.select(".price .amount").text()));
            } else {
                jsonBuilder.field("specialPrice", parsePrice(specialPrice));
                jsonBuilder.field("regularPrice", parsePrice(document.select(".price .amount").text()));
            }


            jsonBuilder.field("description", document.select("#tab-description").text());
            Iterator<Element> meta = document.select("product_meta span").iterator();
            while (meta.hasNext()) {
                Element next = meta.next();
                String name = next.data();
                if (!name.equalsIgnoreCase("categories")) {
                    Elements values = next.select("a");
                    List<String> tmp = new ArrayList<>();
                    for (Element e : values) {
                        tmp.add(e.text());
                    }
                    jsonBuilder.field(name, values);
                }
            }
            jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setJson(jsonBuilder.string());
        }
        product.setWebpageId(webPageEntity.getId());
        result.add(product);
        return result;
    }

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
    public boolean canParse(WebPageEntity webPage) {
        return (webPage.getUrl().startsWith("http://gun-shop.ca/") || webPage.getUrl().startsWith("https://gun-shop.ca/")) && webPage.getType().equals("productPageRaw");
    }
}
