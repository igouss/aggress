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
public class HicalRawProductParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalRawProductParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

        String productName = document.select("h2[itemprop='name']").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            jsonBuilder.field("productName", productName);

            String regularPrice = document.select("#ProductDetails div.DetailRow.PriceRow > div.Value > em").text();
            String specialPrice = document.select("#ProductDetails div.Value > strike").text();
            if ("".equals(specialPrice)) {
                jsonBuilder.field("regularPrice", parsePrice(regularPrice));
            } else {
                jsonBuilder.field("specialPrice", parsePrice(specialPrice));
                jsonBuilder.field("regularPrice", parsePrice(regularPrice));
            }
            jsonBuilder.field("productImage", document.select("#ProductDetails .ProductThumbImage img").attr("src"));
            jsonBuilder.field("description", document.select("#ProductDescription").text().trim());
            jsonBuilder.array("category", webPageEntity.getCategory().split(","));
            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setJson(jsonBuilder.string());
        }
        product.setWebpageId(webPageEntity.getId());
        result.add(product);
        return result;
    }

    /**
     *
     * @param price
     * @return
     */
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
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("productPageRaw");
    }
}
