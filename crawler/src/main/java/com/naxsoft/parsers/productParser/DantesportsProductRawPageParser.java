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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsProductRawPageParser implements ProductParser{
    @Override
    public Set<ProductEntity> parse(WebPageEntity webPage) throws Exception {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPage.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

        Document document = Jsoup.parse(webPage.getContent());
        String productName = document.select(".naitem").text();
        logger.info("Parsing " + productName + ", page=" + webPage.getUrl());

        if (document.select(".outofstock").size() != 0) {
            return products;
        }

        jsonBuilder.field("productName",productName);
//        jsonBuilder.field("category", document.select("div.page.product-details > div.page__header li:nth-child(2) > a").text());
//        jsonBuilder.field("manufacturer", document.select(".product-details__title .product__manufacturer").text());
//
        jsonBuilder.field("productImage", document.select(".itemImgDiv img.itemDetailImg").attr("abs:src"));
//        String regularPriceStrike = document.select("div.product-details__main .product__price del").text();
//        if ("".equals(regularPriceStrike)) {
//            jsonBuilder.field("regularPrice", parsePrice(document.select("div.product-details__main .product__price").text()));
//        } else {
//            jsonBuilder.field("regularPrice", parsePrice(regularPriceStrike));
//            jsonBuilder.field("specialPrice", parsePrice(document.select("div.product-details__main .product__price").first().child(0).text()));
//        }
//
        String priceText = document.select(".itemDetailPrice").text().replace("•", " ");
        Matcher matcher = Pattern.compile("(\\d+|,+)+\\.\\d\\d").matcher(priceText);
        if (matcher.find()) {
            jsonBuilder.field("regularPrice", matcher.group().replace(",", ""));
        }
        jsonBuilder.field("description", document.select(".itemDescription").text());

//        Iterator<Element> labels = document.select("table tr span.lang-en").iterator();
//        Iterator<Element> values = document.select("table td span.lang-en").iterator();
////
//        while(labels.hasNext()) {
//            String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_'));
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
