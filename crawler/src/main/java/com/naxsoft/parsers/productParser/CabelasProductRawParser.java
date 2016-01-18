package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductRawParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CabelasProductRawParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
        String productName = document.select("h1.product-heading").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            jsonBuilder.field("productName", productName);
            jsonBuilder.field("category", document.select(".breadcrumbs").text());
            jsonBuilder.field("productImage", document.select("#product-image img").attr("src"));

            Elements specialPrice = document.select(".productDetails-secondary .price-secondary");
            Elements regularPrice = document.select(".productDetails-secondary .price-primary");
            if (!specialPrice.isEmpty()) {
                jsonBuilder.field("regularPrice", parsePrice(regularPrice.text()));
                jsonBuilder.field("specialPrice", parsePrice(specialPrice.text()));
            } else {
                jsonBuilder.field("regularPrice", parsePrice(regularPrice.text()));
            }
            jsonBuilder.field("description", document.select(".productDetails-section .row").text());
            jsonBuilder.field("category", webPageEntity.getCategory());
            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setJson(jsonBuilder.string());
        }
        product.setWebpageId(webPageEntity.getId());
        result.add(product);
        return result;
    }

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
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productPageRaw");
    }
}
