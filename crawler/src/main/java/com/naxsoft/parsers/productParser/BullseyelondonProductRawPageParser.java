package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.Product;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class BullseyelondonProductRawPageParser implements ProductParser {
    @Override
    public Set<Product> parse(WebPageEntity webPageEntity) {
        Logger logger = LoggerFactory.getLogger(BullseyelondonProductRawPageParser.class);
        Set<Product> products = new HashSet<>();
        Product product = new Product();
        product.setId(webPageEntity.getId());
        product.setPropertie("url", webPageEntity.getUrl());
        product.setPropertie("modificationDate", webPageEntity.getModificationDate().toString());
        try {
            Document document = Jsoup.parse(webPageEntity.getContent());
            document.select(".product-name h1").first().children().remove();
            String productName = document.select(".product-name h1").first().text().trim();
            product.setPropertie("productName", productName);
            product.setPropertie("category", document.select(".breadcrumbs .category18 a").text().trim());
            logger.info("Parsing " + productName + ", page=" + webPageEntity.getUrl());
            product.setPropertie("productImage", document.select("#product_addtocart_form > div.product-img-box > p > a > img").attr("src").trim());
            product.setPropertie("regularPrice", getRegularPrice(document));
            product.setPropertie("specialPrice", getSpecialPrice(document));
            try {
                product.setPropertie("freeShipping", getFreeShipping(document));
            } catch (Exception e) {
            }
            product.setPropertie("unitsAvailable", getUnitsAvailable(document));
            product.setPropertie("description1", document.select(".short-description").text().trim());

            Elements table = document.select("#product_tabs_additional_contents");
            for (Element row : table.select("tr")) {
                Elements th = row.select("th");
                Elements td = row.select("td");
                product.setPropertie(th.text(), td.text());
            }
        } catch (Exception e) {
            logger.warn("Failed to parse page="+webPageEntity.getUrl(), e);
        }
        products.add(product);
        return products;
    }

    private String getFreeShipping(Document document) {
        String raw = document.select(".freeShip").first().text();
        Matcher matcher = Pattern.compile("\\w+|\\s+").matcher(raw);
        if (matcher.find()) {
            return "true";
        } else {
            return "false";
        }
    }

    private String getRegularPrice(Document document) {
        String raw = document.select(".regular-price").text().trim();
        Matcher matcher = Pattern.compile("\\d+.\\d+").matcher(raw);
        if (matcher.find()) {
            String value = matcher.group(0);
            return value;
        } else {
            return raw;
        }
    }

    private String getSpecialPrice(Document document) {
        String raw = document.select(".special-price .price").text().trim();
        Matcher matcher = Pattern.compile("\\d+.\\d+").matcher(raw);
        if (matcher.find()) {
            String value = matcher.group(0);
            return value;
        } else {
            return raw;
        }
    }

    private String getUnitsAvailable(Document document) {
        String raw = document.select(".price-box").first().nextElementSibling().text().trim();
        Matcher matcher = Pattern.compile("\\d+").matcher(raw);
        if (matcher.find()) {
            String value = matcher.group(0);
            return value;
        } else {
            return raw;
        }
    }

    @Override
    public boolean canParse(String url, String action) {
        return url.startsWith("http://www.bullseyelondon.com/") && action.equals("productPageRaw");
    }
}
