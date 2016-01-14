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
public class IrungunsRawProductPageParser extends AbstractRawPageParser {
    private static final Logger logger = LoggerFactory.getLogger(IrungunsRawProductPageParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> products = new HashSet<>();
        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

        if (!document.select(".saleImage").isEmpty()) {
            return products;
        }

        String productName = document.select("div.innercontentDiv > div > div > h2").text();

        if (productName.isEmpty()) {
            return products;
        }

        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());
        jsonBuilder.field("productName", productName);
        String manufacturer = document.select(".product-details__title .product__manufacturer").text();
        if (!manufacturer.isEmpty()) {
            jsonBuilder.field("manufacturer", manufacturer);
        }
        String productImage = document.select("div.imgLiquidNoFill a").attr("abs:src");
        if (productImage.isEmpty()) {
            productImage = document.select(".es-carousel img").attr("abs:src");
        }
        jsonBuilder.field("productImage", productImage);
        jsonBuilder.field("regularPrice", parsePrice(document.select("#desPrice > li:nth-child(1) > span.pricetag.show").text()));
        String specialPrice = document.select("#desPrice > li:nth-child(2) > span.pricetag.show").text();
        if (!specialPrice.isEmpty()) {
            jsonBuilder.field("specialPrice", parsePrice(specialPrice));
        }
        String description = document.select("#TabbedPanels1 > div > div:nth-child(1)").text();
        if (!description.isEmpty()) {
            jsonBuilder.field("description", description);
        }
        jsonBuilder.field("category", webPageEntity.getCategory());
        Iterator<Element> labels = document.select("table.productTbl > tbody > tr > td:nth-child(1)").iterator();
        Iterator<Element> values = document.select("table.productTbl > tbody > tr > td:nth-child(2)").iterator();

        while (labels.hasNext()) {
            String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_').replace(":", "").trim());
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

    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            try {
                return matcher.group(1).replace(",", "");
//                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
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
