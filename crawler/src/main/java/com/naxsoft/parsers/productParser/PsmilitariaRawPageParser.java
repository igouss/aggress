package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.eventbus.Message;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class PsmilitariaRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaRawPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$\\s?((\\d+|,)+\\s?\\.\\d+)");

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
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

            Elements products = document.select("body > p");

            for (Element el : products) {
                String elText = el.text().trim();
                elText = elText.replace((char) 160, (char) 32);
                if (elText.trim().isEmpty() || elText.contains("mainpage") || elText.contains("Main Page.") || elText.trim().equals(",")) {
                    continue;
                }
                String productName = elText;
                LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

                ProductEntity product;
                try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                    jsonBuilder.startObject();
                    jsonBuilder.field("url", webPageEntity.getUrl());
                    jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                    jsonBuilder.field("productName", productName);
                    String price = parsePrice(webPageEntity, productName);
                    if (!price.isEmpty()) {
                        jsonBuilder.field("regularPrice", price);
                    }
                    jsonBuilder.field("category", webPageEntity.getCategory().split(","));
                    jsonBuilder.endObject();
                    product = new ProductEntity(jsonBuilder.string(), webPageEntity.getUrl());
                }
                result.add(product);

            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("psmilitaria.50megs.com") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("psmilitaria.50megs.com/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
