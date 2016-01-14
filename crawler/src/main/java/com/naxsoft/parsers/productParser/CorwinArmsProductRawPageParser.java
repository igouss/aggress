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
public class CorwinArmsProductRawPageParser extends AbstractRawPageParser {
    private static final Logger logger = LoggerFactory.getLogger(CorwinArmsProductRawPageParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
        String productName = document.select("#maincol h1").text();
        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
        jsonBuilder.field("productName", productName);
        String img = document.select("div.field-type-image img").attr("abs:src");
        if (img.isEmpty()) {
            img = document.select("div.field-name-field-product-mag-image > div > div > img").attr("abs:src");
        }
        jsonBuilder.field("productImage", img);
        jsonBuilder.field("regularPrice", parsePrice(document.select(".field-name-commerce-price").text()));
        jsonBuilder.field("description", document.select("div.field-type-text-with-summary > div > div").text().replace("\u0160", "\n"));
        jsonBuilder.endObject();
        product.setUrl(webPageEntity.getUrl());
        product.setJson(jsonBuilder.string());
        product.setWebpageId(webPageEntity.getId());
        result.add(product);

        return result;

    }

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
        return webPage.getUrl().startsWith("https://www.corwin-arms.com/") && webPage.getType().equals("productPageRaw");
    }
}
