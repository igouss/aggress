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
public class GunshopRawPageParser extends AbstractRawPageParser {
    private static final Logger logger = LoggerFactory.getLogger(GunshopRawPageParser.class);

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
        Document document = Jsoup.parse(fromZip(webPageEntity.getContent()), webPageEntity.getUrl());


        String productName = document.select("#page-heading  h1").text();
        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
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


        jsonBuilder.field("description", document.select(".tagged_as a").text());
//        jsonBuilder.field("description", document.select("#tab-description").text());
        jsonBuilder.endObject();
        product.setUrl(webPageEntity.getUrl());
        product.setJson(jsonBuilder.string());
        product.setWebpageId(webPageEntity.getId());
        result.add(product);
        return result;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://gun-shop.ca/") && webPage.getType().equals("productPageRaw");
    }
}
