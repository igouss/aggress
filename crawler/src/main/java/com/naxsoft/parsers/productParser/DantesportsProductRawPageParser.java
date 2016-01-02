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
public class DantesportsProductRawPageParser extends AbstractRawPageParser implements ProductParser {
    private static final Logger logger = LoggerFactory.getLogger(DantesportsProductRawPageParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPage) throws Exception {
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPage.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

        Document document = Jsoup.parse(fromZip(webPage.getContent()), webPage.getUrl());
        String productName = document.select(".naitem").text();
        logger.info("Parsing {}, page={}", productName, webPage.getUrl());

        if (!document.select(".outofstock").isEmpty()) {
            return products;
        }

        jsonBuilder.field("productName", productName);
        jsonBuilder.field("productImage", document.select(".itemImgDiv img.itemDetailImg").attr("abs:src"));

        String priceText = document.select(".itemDetailPrice").text().replace("\\xEF\\xBF\\xBD", " ");
        Matcher matcher = Pattern.compile("(\\d+|,+)+\\.\\d\\d").matcher(priceText);
        if (matcher.find()) {
            jsonBuilder.field("regularPrice", matcher.group().replace(",", ""));
        }
        jsonBuilder.field("description", document.select(".itemDescription").text());
//        Iterator<Element> labels = document.select("table tr span.lang-en").iterator();
//        Iterator<Element> values = document.select("table td span.lang-en").iterator();
//        while(labels.hasNext()) {
//            String specName = labels.next().text();
//            String specValue = values.next().text();
//            jsonBuilder.field(specName, specValue);
//        }
        jsonBuilder.endObject();
        product.setUrl(webPage.getUrl());
        product.setWebpageId(webPage.getId());
        product.setJson(jsonBuilder.string());
        products.add(product);
        return products;

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("productPageRaw");
    }
}
