package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class MarstarRawProductPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarRawProductPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("((\\d+(\\.|,))+\\d\\d)+");

    public MarstarRawProductPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    /**
     * @param webPageEntity
     * @return
     */
    private static String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (null != category) {
            return category.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    /**
     * @param price
     * @return
     */
    private static ArrayList<String> parsePrice(String price) {
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = pricePattern.matcher(price);


        while (matcher.find()) {
            result.add(matcher.group(1).replace(",", ""));
        }
        return result;
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

            productName = document.select("h1").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            productImage = document.select("img[id=mainPic]").attr("abs:src");
            category = getNormalizedCategories(webPageEntity);
            ArrayList<String> price = parsePrice(document.select(".priceAvail").text());
            if (price.isEmpty()) {
                return Observable.empty();
            } else if (1 == price.size()) {
                regularPrice = price.get(0);
            } else {
                regularPrice = price.get(0);
                specialPrice = price.get(1);
            }

            description = document.select("#main-content > div:nth-child(7)").text();
            if (description.isEmpty()) {
                description = document.select("#main-content > div:nth-child(6), #main-content > div:nth-child(8)").text();
            }

            Iterator<Element> labels = document.select("#main-content > table > tbody > tr:nth-child(1) > th").iterator();
            Iterator<Element> values = document.select("#main-content > table > tbody > tr.baseTableCell > td").iterator();

            while (labels.hasNext()) {
                String specName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, labels.next().text().replace(' ', '_'));
                String specValue = values.next().text();
                attr.put(specName, specValue);
            }

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result)
                .doOnNext(e -> parseResultCounter.inc());
    }

    @Override
    String getSite() {
        return "marstar.ca";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
