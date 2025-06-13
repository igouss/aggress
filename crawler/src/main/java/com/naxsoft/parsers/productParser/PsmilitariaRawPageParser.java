package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class PsmilitariaRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaRawPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$\\s?((\\d+|,)+\\s?\\.\\d+)");

    public PsmilitariaRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "").replace(" ", "");
        } else {
            LOGGER.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return "";
        }
    }

    @Override
    public Flux<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            Elements products = document.select("body > p");

            for (Element el : products) {

                ProductEntity product;
                String productName = null;
                String url = null;
                String regularPrice = null;
                String specialPrice = null;
                String productImage = null;
                String description = null;
                Map<String, String> attr = new HashMap<>();
                String[] category = null;

                String elText = el.text().trim();
                elText = elText.replace((char) 160, (char) 32);
                if (elText.trim().isEmpty() || elText.contains("mainpage") || elText.contains("Main Page.") || elText.trim().equals(",")) {
                    continue;
                }
                productName = elText;
                LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


                url = webPageEntity.getUrl();
                Element img = el.previousElementSibling().select("img").first();
                if (img != null) {
                    productImage = img.attr("src").trim();
                }

                String price = parsePrice(webPageEntity, productName);
                if (!price.isEmpty()) {
                    regularPrice = price;
                }
                category = webPageEntity.getCategory().split(",");

                product = ProductEntity.legacyCreate(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
                result.add(product);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Flux.fromIterable(result)
                .doOnNext(e -> parseResultCounter.inc());
    }

    @Override
    String getSite() {
        return "psmilitaria.50megs.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
