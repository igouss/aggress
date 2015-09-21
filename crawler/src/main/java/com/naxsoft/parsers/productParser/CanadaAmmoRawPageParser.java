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
public class CanadaAmmoRawPageParser implements ProductParser {
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

        if (document.select(".product-details__add").size() == 0) {
            return products;
        }


        String productName = document.select(".product-details__title .product__name").text();
        logger.info("Parsing " + productName + ", page=" + webPageEntity.getUrl());

        jsonBuilder.field("productName",productName);
        jsonBuilder.field("category", document.select("div.page.product-details > div.page__header li:nth-child(2) > a").text());
        jsonBuilder.field("manufacturer", document.select(".product-details__title .product__manufacturer").text());

        jsonBuilder.field("productImage", document.select("img[itemprop=image]").attr("srcset"));
        String regularPriceStrike = document.select("div.product-details__main .product__price del").text();
        if ("".equals(regularPriceStrike)) {
            jsonBuilder.field("regularPrice", parsePrice(document.select("div.product-details__main .product__price").text()));
        } else {
            jsonBuilder.field("regularPrice", parsePrice(regularPriceStrike));
            jsonBuilder.field("specialPrice", parsePrice(document.select("div.product-details__main .product__price").first().child(0).text()));
        }

        jsonBuilder.field("description", document.select("div.product-details__meta-wrap > div > div > div:nth-child(1) > section span").text());

        Iterator<Element> labels = document.select(".product-details__spec-label").iterator();
        Iterator<Element> values = document.select(".product-details__spec-value").iterator();

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
                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception e) {
                return Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            return price;
        }
    }


    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("productPageRaw");
    }
}
