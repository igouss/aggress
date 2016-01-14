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
public class EllwoodeppsRawProductParser extends AbstractRawPageParser {
    private static final Logger logger = LoggerFactory.getLogger(EllwoodeppsRawProductParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

        if (!document.select(".firearm-links-sold").isEmpty()) {
            return result;
        }

        String productName = document.select(".product-name span").text();
        logger.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

        ProductEntity product = new ProductEntity();
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", webPageEntity.getUrl());
        jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
        jsonBuilder.field("productName", productName);

        jsonBuilder.field("regularPrice", parsePrice(document.select(".price").text()));

        Iterator<Element> labels = document.select("th.label").iterator();
        Iterator<Element> values = document.select("td.data").iterator();

        while (labels.hasNext()) {
            String specName = labels.next().text();
            String specValue = values.next().text();
            if (!specValue.isEmpty()) {
                jsonBuilder.field(specName, specValue);
            }
        }
        jsonBuilder.field("category", webPageEntity.getCategory());
        jsonBuilder.endObject();
        product.setUrl(webPageEntity.getUrl());
        product.setJson(jsonBuilder.string());
        product.setWebpageId(webPageEntity.getId());
        result.add(product);
        return result;
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
        return webPage.getUrl().startsWith("https://ellwoodepps.com/") && webPage.getType().equals("productPageRaw");
    }
}
