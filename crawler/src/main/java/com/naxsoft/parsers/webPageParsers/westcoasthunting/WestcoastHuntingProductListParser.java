package com.naxsoft.parsers.webPageParsers.westcoasthunting;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class WestcoastHuntingProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestcoastHuntingProductListParser.class);

    public WestcoastHuntingProductListParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Flux<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        return Flux.create(emitter -> {
            try {
                Document document = downloadResult.getDocument();
                if (document != null) {
                    WebPageEntity sourcePage = downloadResult.getSourcePage();

                    // Sub- categories
                    Elements elements = document.select(".product-category > a");
                    for (Element element : elements) {
                        WebPageEntity webPageEntity = WebPageEntity.legacyCreate(downloadResult.getSourcePage(), "", "productList", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                        LOGGER.info("Product sub-listing {}", webPageEntity.getUrl());
                        emitter.next(webPageEntity);
                    }

                    // Pagination
                    if (!sourcePage.getUrl().contains("/page/")) {
                        elements = document.select("a.page-numbers");
                        int max = 0;
                        for (Element e : elements) {
                            try {
                                max = Integer.parseInt(e.text());
                            } catch (Exception ignore) {
                            }
                        }
                        for (int i = 2; i < max; i++) {
                            WebPageEntity webPageEntity = WebPageEntity.legacyCreate(sourcePage, "", "productList", sourcePage.getUrl() + "page/" + i + "/", sourcePage.getCategory());
                            LOGGER.info("Product list subpage {} {}", i, webPageEntity.getUrl());
                            emitter.next(webPageEntity);
                        }
                    }

                    // Product pages
                    elements = document.select(".product.instock a");
                    for (Element el : elements) {
                        WebPageEntity webPageEntity = WebPageEntity.legacyCreate(sourcePage, "", "productPage", el.attr("abs:href"), sourcePage.getCategory());
                        LOGGER.info("Product page {}", webPageEntity.getUrl());
                        emitter.next(webPageEntity);
                    }

                    emitter.complete();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse", e);
                emitter.complete();
            }

        }, FluxSink.OverflowStrategy.BUFFER);
    }

    @Override
    public Flux<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity))
                .flatMap(this::parseDocument)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "westcoasthunting.ca";
    }

}
