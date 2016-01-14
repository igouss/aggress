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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class WholesalesportsProductRawPageParser extends AbstractRawPageParser {
    private static final Logger logger = LoggerFactory.getLogger(WholesalesportsProductRawPageParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
        String productName = document.select("h1.product-name").text();
        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        if (2 == document.select("div.alert.negative").size()) {
            return result;
        }

        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
        jsonBuilder.field("productName", productName);
        jsonBuilder.field("productImage", document.select(".productImagePrimaryLink img").attr("abs:src"));
        jsonBuilder.field("manufacturer", document.select(".product-brand").text());
        jsonBuilder.field("category", webPageEntity.getCategory());
        if (document.select(".new .price-value").isEmpty()) {
            Elements price = document.select(".current .price-value");
            if (!price.isEmpty()) {
                jsonBuilder.field("regularPrice", parsePrice(price.text()));
            } else {
                price = document.select("div.productDescription span.price-value");
                jsonBuilder.field("regularPrice", parsePrice(price.text()));
            }
        } else {
            jsonBuilder.field("regularPrice", parsePrice(document.select(".old .price-value").text()));
            jsonBuilder.field("specialPrice", parsePrice(document.select(".new .price-value").text()));
        }
        jsonBuilder.field("description", document.select(".summary").text());
        jsonBuilder.endObject();
        product.setUrl(webPageEntity.getUrl());
        product.setJson(jsonBuilder.string());
        product.setWebpageId(webPageEntity.getId());
        result.add(product);
        return result;
    }

    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            return price;
        }
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.wholesalesports.com/") && webPage.getType().equals("productPageRaw");
    }
}
