package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class FrontierfirearmsRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierfirearmsRawPageParser.class);

    /**
     *
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

        if (!document.select(".firearm-links-sold").isEmpty()) {
            return result;
        }

        String productName = document.select(".product-name h1").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

            jsonBuilder.field("productName", productName);
            jsonBuilder.field("productImage", document.select(".product-img-box img").attr("src"));

            String specialPrice = document.select(".special-price .price").text();
            if ("".equals(specialPrice)) {
                jsonBuilder.field("regularPrice", parsePrice(document.select(".regular-price .price").text()));
            } else {
                jsonBuilder.field("specialPrice", parsePrice(specialPrice));
                jsonBuilder.field("regularPrice", parsePrice(document.select(".old-price .price").text()));
            }
//        jsonBuilder.field("description", document.select(".short-description").text());
            jsonBuilder.field("description", document.select("#product_tabs_description_tabbed_contents > div").text());
            jsonBuilder.field("category", webPageEntity.getCategory());
            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setJson(jsonBuilder.string());
        }
        product.setWebpageId(webPageEntity.getId());
        result.add(product);
        return result;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://frontierfirearms.ca/") && webPage.getType().equals("productPageRaw");
    }
}
