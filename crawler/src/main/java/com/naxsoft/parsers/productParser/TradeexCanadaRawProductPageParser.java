package com.naxsoft.parsers.productParser;

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
public class TradeexCanadaRawProductPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaRawProductPageParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

        String productName = document.select("h1.title").text();
        if (productName.toUpperCase().contains("OUT OF STOCK") || productName.contains("Donation to the CSSA")) {
            return result;
        }
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            jsonBuilder.field("productName", productName);
            jsonBuilder.field("productImage", document.select(".main-product-image img").attr("abs:src"));
            jsonBuilder.field("description", document.select(".product-body").text());
            jsonBuilder.field("regularPrice", parsePrice(document.select("#price-group .product span").text()));
            String allCategories = webPageEntity.getCategory();
            if (allCategories != null) {
                jsonBuilder.array("category", allCategories.split(","));
            }
            if (allCategories != null) {
                jsonBuilder.array("category", allCategories.split(","));
            }
            Iterator<Element> labels = document.select(".product-additional .field-label").iterator();
            Iterator<Element> values = document.select(".product-additional .field-items").iterator();

            while (labels.hasNext()) {
                String specName = labels.next().text().replace(":", "").trim();
                String specValue = values.next().text();
                jsonBuilder.field(specName, specValue);
            }

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
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("productPageRaw");
    }
}
