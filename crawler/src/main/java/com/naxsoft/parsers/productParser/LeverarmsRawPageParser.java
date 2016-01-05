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
public class LeverarmsRawPageParser extends AbstractRawPageParser {
    private static final Logger logger = LoggerFactory.getLogger(LeverarmsRawPageParser.class);

    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("\\$((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            return price;
        }
    }

    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

        Document document = Jsoup.parse(fromZip(webPageEntity.getContent()), webPageEntity.getUrl());

        String productName = document.select(".product-shop .product-name").text();
        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        if (document.select("p.availability.in-stock").isEmpty()) {
            return products;
        }


        jsonBuilder.field("productName", productName);

        jsonBuilder.field("productImage", document.select(".product-img-box img").attr("abs:src"));

        Elements specialPrice = document.select(".special-price .price");
        if (!specialPrice.isEmpty()) {
            jsonBuilder.field("regularPrice", parsePrice(document.select(".old-price .price").text()));
            jsonBuilder.field("specialPrice", parsePrice(specialPrice.text()));
        } else {
            jsonBuilder.field("regularPrice", parsePrice(document.select(".regular-price .price").text()));
        }

        jsonBuilder.field("description", removeNonASCII(document.select(".product-collateral").text() + " " + document.select(".short-description").text()));

        jsonBuilder.endObject();
        product.setUrl(webPageEntity.getUrl());
        product.setWebpageId(webPageEntity.getId());
        product.setJson(jsonBuilder.string());
        products.add(product);
        return products;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.leverarms.com/") && webPage.getType().equals("productPageRaw");
    }
}
