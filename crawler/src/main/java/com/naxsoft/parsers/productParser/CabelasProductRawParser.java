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
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasProductRawParser implements ProductParser {
    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet result = new HashSet();
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Document document = Jsoup.parse(webPageEntity.getContent());
        String productName = document.select("h1.product-heading").text();
        logger.info("Parsing " + productName + ", page=" + webPageEntity.getUrl());


        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", "https://www.wolverinesupplies.com/ProductDetail/" + webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
        jsonBuilder.field("productName", productName);
        jsonBuilder.field("category", document.select(".breadcrumbs").text());
        jsonBuilder.field("productImage", document.select("#product-image img").attr("src"));
        jsonBuilder.field("regularPrice", parsePrice(document.select(".productDetails-secondary .price-primary").text()));
        jsonBuilder.field("specialPrice", parsePrice(document.select(".productDetails-secondary .price-secondary").text()));
        jsonBuilder.field("description1", document.select("productDetails-section").text());
        jsonBuilder.endObject();
        product.setJson(jsonBuilder.string());
        product.setWebpageId(webPageEntity.getId());
        result.add(product);
        return result;
    }

    private String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            try {
                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception e) {
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
