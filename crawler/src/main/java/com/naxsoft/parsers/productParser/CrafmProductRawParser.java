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
public class CrafmProductRawParser extends AbstractRawPageParser {
    private static final Logger logger = LoggerFactory.getLogger(CrafmProductRawParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
        String productName = document.select(".product-essential .product-name").text();
        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
        jsonBuilder.field("productName", productName);
        String img = document.select(".product-image img").attr("src");
        jsonBuilder.field("productImage", img);
        jsonBuilder.field("regularPrice", parsePrice(document.select("#product_addtocart_form > div.product-shop > div:nth-child(4) > h2 > span").text()));
        jsonBuilder.field("description", document.select("div.short-description p[align=justify]").text());
        jsonBuilder.field("category", webPageEntity.getCategory());
        jsonBuilder.endObject();
        product.setUrl(webPageEntity.getUrl());
        product.setJson(jsonBuilder.string());
        product.setWebpageId(webPageEntity.getId());
        result.add(product);

        return result;

    }

    /**
     *
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            return price;
        }
    }


    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.crafm.com/") && webPage.getType().equals("productPageRaw");
    }
}
