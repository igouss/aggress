package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.reactivex.Flowable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Copyright NAXSoft 2015
 */
class CanadiangunnutzRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzRawPageParser.class);

    public CanadiangunnutzRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public Flowable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            ProductEntity product;
            String productName;
            String url;
            String regularPrice = null;
            String specialPrice = null;
            String productImage = null;
            String description;
            Map<String, String> attr = new HashMap<>();
            String[] category;


            url = webPageEntity.getUrl();
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            productName = document.select("div.postdetails h2").text();
            if (productName.isEmpty()) {
                productName = document.select("div.postbody h2.title").text();
            }

            if (productName.toLowerCase().contains("sold") || productName.toLowerCase().contains("remove") || productName.toLowerCase().contains("delete")) {
                return Flowable.empty();
            }
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            Elements images = document.select(".content blockquote img");
            if (!images.isEmpty() && !images.first().attr("src").contains("images/smilies")) {
                productImage = images.first().attr("abs:src");
            } else {
                images = document.select(".content blockquote img");
                boolean found = false;
                for (Element el : images) {
                    if (el.attr("src").contains("photobucket")) {
                        productImage = el.attr("abs:src");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    images = document.select(".content blockquote a[href]");
                    if (!images.isEmpty()) {
                        for (Element el : images) {
                            if (el.attr("href").endsWith("jpg")) {
                                productImage = el.attr("href");
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    images = document.select(".content img.attach");
                    if (!images.isEmpty()) {
                        productImage = images.first().attr("abs:src");
                    }
                }
            }
            description = document.select("div.postdetails  div.postrow.has_after_content .content").text();
            category = getNormalizedCategories(webPageEntity);

            product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Flowable.fromIterable(result);
    }

    /**
     * @param webPageEntity
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (category != null) {
            return category.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }


    @Override
    String getSite() {
        return "canadiangunnutz.com";
    }
}
