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
public class IrungunsRawProductPageParser implements ProductParser {
    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

        Document document = Jsoup.parse(webPageEntity.getContent());

        if (document.select(".saleImage").size() != 0) {
            return products;
        }

        String productName = document.select("div.innercontentDiv > div > div > h2").text();
        logger.info("Parsing " + productName + ", page=" + webPageEntity.getUrl());
        jsonBuilder.field("productName",productName);
        jsonBuilder.field("manufacturer", document.select(".product-details__title .product__manufacturer").text());
        jsonBuilder.field("productImage", document.select("div.imgLiquidNoFill a").attr("abs:src"));
        jsonBuilder.field("regularPrice", parsePrice(document.select("#desPrice > li:nth-child(1) > span.pricetag.show").text()));
        jsonBuilder.field("specialPrice", parsePrice(document.select("#desPrice > li:nth-child(2) > span.pricetag.show").text()));
        jsonBuilder.field("description", document.select("#TabbedPanels1 > div > div:nth-child(1)").text());
        Iterator<Element> labels = document.select("table.productTbl > tbody > tr > td:nth-child(1)").iterator();
        Iterator<Element> values = document.select("table.productTbl > tbody > tr > td:nth-child(2)").iterator();

        while(labels.hasNext()) {
            String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_'));
            String specValue = values.next().text();
            jsonBuilder.field(specName, specValue);
        }
        jsonBuilder.endObject();
        product.setUrl(webPageEntity.getUrl());
        product.setWebpageId(webPageEntity.getId());
        product.setJson(jsonBuilder.string());
        products.add(product);
        return products;

    }

    private String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            try {
                return matcher.group(1).replace(",","");
//                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception e) {
                return Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            return price;
        }
    }


    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.irunguns.us/") && webPage.getType().equals("productPageRaw");
    }
}
