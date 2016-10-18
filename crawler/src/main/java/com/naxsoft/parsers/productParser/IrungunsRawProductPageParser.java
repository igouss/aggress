package com.naxsoft.parsers.productParser;

import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.eventbus.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class IrungunsRawProductPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(IrungunsRawProductPageParser.class);

    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("Optics", "optic");
        mapping.put("Ammunition", "ammo");
        mapping.put("Previously Enjoyed Guns & Accessories", "firearm,misc");
        mapping.put("Rifles", "firearm");
        mapping.put("shotgun", "firearm");
        mapping.put("Handguns", "firearm");
        mapping.put("Antiques", "firearm");
        mapping.put("Parts & Gear", "firearm,misc");
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            try {
                return matcher.group(1).replace(",", "");
//                return NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).toString();
            } catch (Exception ignored) {
                return Double.valueOf(matcher.group(1)).toString();
            }
        } else {
            LOGGER.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return price;
        }
    }

    @Override
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            ProductEntity product;
            String productName = null;
            String url = null;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description = null;
            Map<String, String> attr = new HashMap<>();
            String[] category = null;

            url = webPageEntity.getUrl();

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            if (!document.select(".saleImage").isEmpty()) {
                return Observable.empty();
            }

            productName = document.select("div.innercontentDiv > div > div > h2").text();
            if (productName.isEmpty()) {
                return Observable.empty();
            }

            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            String manufacturer = document.select(".product-details__title .product__manufacturer").text();
            if (!manufacturer.isEmpty()) {
                attr.put("manufacturer", manufacturer);
            }
            productImage = document.select("div.imgLiquidNoFill a").attr("abs:src");
            if (productImage.isEmpty()) {
                productImage = document.select(".es-carousel img").attr("abs:src");
            }

            regularPrice = parsePrice(webPageEntity, document.select("#desPrice > li:nth-child(1) > span.pricetag.show").text());
            specialPrice = document.select("#desPrice > li:nth-child(2) > span.pricetag.show").text();
            if (!specialPrice.isEmpty()) {
                specialPrice = parsePrice(webPageEntity, specialPrice);
            }
            description = document.select("#TabbedPanels1 > div > div:nth-child(1)").text();

            Iterator<Element> labels = document.select("table.productTbl > tbody > tr > td:nth-child(1)").iterator();
            Iterator<Element> values = document.select("table.productTbl > tbody > tr > td:nth-child(2)").iterator();

            while (labels.hasNext()) {
                String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_').replace(":", "").trim());
                String specValue = values.next().text();
                if (specName.contains("Department")) {
                    category = getNormalizedCategories(webPageEntity, specValue);
                } else {
                    attr.put(specName, specValue);
                }
            }

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result);
    }

    /**
     * @param webPageEntity
     * @param category
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity, String category) {
        if (mapping.containsKey(category)) {
            return mapping.get(category).split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("irunguns.us") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("irunguns.us/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
