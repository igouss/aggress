package com.naxsoft.parsers.webPageParsers.wolverinegt;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

class WolverinegFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinegFrontPageParser.class);

    public WolverinegFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".product-wrapper h3 a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");
                if (null != linkUrl && !linkUrl.isEmpty()) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", linkUrl, downloadResult.getSourcePage().getCategory());
                    LOGGER.info("ProductPageUrl={}", linkUrl);
                    result.add(webPageEntity);
                }
            }
        }
        return result.build();
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        Set<WebPageEntity> webPageEntities = ImmutableSet.<WebPageEntity>builder()
                .add(create(parent, "http://wolverinegt.ca/product-category/firearms/hand-guns/?products-per-page=all", "firearm"))
                .add(create(parent, "http://wolverinegt.ca/product-category/firearms/shot-guns/?products-per-page=all", "firearm"))
                .add(create(parent, "http://wolverinegt.ca/product-category/firearms/rifles/?products-per-page=all", "firearm"))
                .build();

        return Flowable.fromIterable(webPageEntities)
                .observeOn(Schedulers.io())
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMapIterable(this::parseDocument)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "wolverinegt.ca";
    }

}
