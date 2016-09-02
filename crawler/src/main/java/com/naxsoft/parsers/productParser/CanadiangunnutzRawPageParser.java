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

/**
 * Copyright NAXSoft 2015
 */
class CanadiangunnutzRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzRawPageParser.class);

    @Override
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();
        try {
            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

                Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

                String productName = document.select("div.postdetails h2").text();
                if (productName.isEmpty()) {
                    productName = document.select("div.postbody h2.title").text();
                }

                if (productName.toLowerCase().contains("sold") || productName.toLowerCase().contains("remove") || productName.toLowerCase().contains("delete")) {
                    return Observable.empty();
                }
                LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

                jsonBuilder.field("productName", productName);

                Elements images = document.select(".content blockquote img");
                if (!images.isEmpty() && !images.first().attr("src").contains("images/smilies")) {
                    jsonBuilder.field("productImage", images.first().attr("abs:src"));
                } else {
                    images = document.select(".content blockquote img");
                    boolean found = false;
                    for (Element el : images) {
                        if (el.attr("src").contains("photobucket")) {
                            jsonBuilder.field("productImage", el.attr("abs:src"));
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        images = document.select(".content blockquote a[href]");
                        if (!images.isEmpty()) {
                            for (Element el : images) {
                                if (el.attr("href").endsWith("jpg")) {
                                    jsonBuilder.field("productImage", el.attr("href"));
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!found) {
                        images = document.select(".content img.attach");
                        if (!images.isEmpty()) {
                            jsonBuilder.field("productImage", images.first().attr("abs:src"));
                        }
                    }
                }
                jsonBuilder.field("description", document.select("div.postdetails  div.postrow.has_after_content .content").text());
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
                jsonBuilder.endObject();
                product.setUrl(webPageEntity.getUrl());
                product.setWebpageId(webPageEntity.getId());
                product.setJson(jsonBuilder.string());
            }
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result);
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
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("canadiangunnutz.com") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("canadiangunnutz.com/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
