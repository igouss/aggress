package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
class CanadiangunnutzRawPageParser extends AbstractRawPageParser {
    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
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
            productName = document.select("div.postdetails h2").text();
            if (productName.isEmpty()) {
                productName = document.select("div.postbody h2.title").text();
            }

            if (productName.toLowerCase().contains("sold") || productName.toLowerCase().contains("remove") || productName.toLowerCase().contains("delete")) {
                return result;
            }
            log.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

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
            log.error("Failed to parse: {}", webPageEntity, e);
        }
        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (category != null) {
            return category.split(",");
        }
        log.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }


    @Override
    String getSite() {
        return "canadiangunnutz.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
