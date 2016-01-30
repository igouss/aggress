package com.naxsoft.parsers.productParser;

import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class AlflahertysRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlflahertysRawPageParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
        parseDocument(webPageEntity, result, document);
        return result;
    }

    private void parseDocument(WebPageEntity webPageEntity, HashSet<ProductEntity> result, Document document) throws IOException {
        String productName = document.select(".product_name").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        if (!document.select(".product_section .sold_out").text().equals("Sold Out")) {
            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select("meta[property=og:image]").attr("content"));

                if (document.select(".product_section .was_price").text().equals("")) {
                    jsonBuilder.field("regularPrice", parsePrice(document.select(".product_section .current_price").text()));
                } else {
                    jsonBuilder.field("regularPrice", parsePrice(document.select(".product_section .was_price").text()));
                    jsonBuilder.field("specialPrice", parsePrice(document.select(".product_section-secondary .price-current_price").text()));
                }
                jsonBuilder.field("description", document.select(".product_section .description").text());
                String allCategories = webPageEntity.getCategory();
                if (allCategories != null) {
                    jsonBuilder.array("category", allCategories.split(","));
                }
                Iterator<Element> labels = document.select(".meta span:nth-child(1)").iterator();
                Iterator<Element> values = document.select(".meta span:nth-child(2)").iterator();
                while (labels.hasNext()) {
                    String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_').replace(":", "").trim());
                    String specValue = values.next().text();
                    jsonBuilder.field(specName, specValue);
                }
                jsonBuilder.endObject();
                product.setUrl(document.location());
                product.setJson(jsonBuilder.string());
            }
            product.setWebpageId(webPageEntity.getId());
            result.add(product);
        }
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
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("productPageRaw");
    }
}
