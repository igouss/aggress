package com.naxsoft.parsers.productParser;

import com.google.common.base.CaseFormat;
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
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzRawPageParser implements ProductParser {
    private static final Logger logger = LoggerFactory.getLogger(CanadiangunnutzRawPageParser.class);

    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

        String productName = document.select("div.postdetails h2").text().replaceAll("[^\\x00-\\x7F]", "");
        if (productName.toLowerCase().contains("sold") || productName.toLowerCase().contains("remove")) {
            return products;
        }
        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        jsonBuilder.field("productName",productName);

        Elements images = document.select(".content blockquote img");
        if (!images.isEmpty()) {
            jsonBuilder.field("productImage", images.first().attr("abs:src"));
        }
        jsonBuilder.field("description", document.select(".content blockquote").text());

        jsonBuilder.endObject();
        product.setUrl(webPageEntity.getUrl());
        product.setWebpageId(webPageEntity.getId());
        product.setJson(jsonBuilder.string());
        products.add(product);
        return products;
    }


    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("productPageRaw");
    }
}
