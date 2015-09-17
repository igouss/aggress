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
public class AlflahertysRawPageParser implements ProductParser {
    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet result = new HashSet();
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Document document = Jsoup.parse(webPageEntity.getContent());
        String productName = document.select(".product_name").text();
        logger.info("Parsing " + productName + ", page=" + webPageEntity.getUrl());

        if(!document.select(".product_section .sold_out").text().equals("Sold Out")) {
            ProductEntity product = new ProductEntity();
            XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
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
            Iterator<Element> labels = document.select(".meta span:nth-child(1)").iterator();
            Iterator<Element> values = document.select(".meta span:nth-child(2)").iterator();
            while(labels.hasNext()) {
                String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_'));
                String specValue = values.next().text();
                jsonBuilder.field(specName, specValue);
            }
            jsonBuilder.endObject();
            product.setJson(jsonBuilder.string());
            product.setWebpageId(webPageEntity.getId());
            result.add(product);
        }
        return result;
    }

    private String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
//            try {
//                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
//            } catch (Exception e) {
//                return Double.valueOf(matcher.group(1)).toString();
//            }
        } else {
            return price;
        }
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("productPageRaw");
    }
}
