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
public class TheammosourceRawPageParser extends AbstractRawPageParser {
    private static final Logger logger = LoggerFactory.getLogger(TheammosourceRawPageParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Document document = Jsoup.parse(fromZip(webPageEntity.getContent()), webPageEntity.getUrl());

        String productName = document.select("#productListHeading").text();
        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        if (document.select("#productDetailsList > li:nth-child(2)").text().equals("0 Units in Stock")) {
            return result;
        }

        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
        jsonBuilder.field("productName", productName);
        jsonBuilder.field("productImage", document.select("#productMainImage img").attr("abs:src"));
        jsonBuilder.field("description", document.select("#productDescription").text());

        Elements specialPrice = document.select("#productPrices .productSpecialPrice");
        if (!specialPrice.isEmpty()) {
            jsonBuilder.field("regularPrice", parsePrice(document.select("#productPrices .normalprice").text()));
            jsonBuilder.field("specialPrice", parsePrice(specialPrice.text()));
        } else {
            jsonBuilder.field("regularPrice", parsePrice(document.select("#productPrices #retail").text()));
        }

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
        return webPage.getUrl().startsWith("http://www.theammosource.com/") && webPage.getType().equals("productPageRaw");
    }
}