package com.naxsoft.parsers.webPageParsers.frontierfirearms;

import java.util.HashSet;
import java.util.Set;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class FrontierfirearmsProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierfirearmsProductListParser.class);

    public FrontierfirearmsProductListParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".ProductDetails a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productPage", element.attr("abs:href"),
                        downloadResult.getSourcePage().getCategory());
                LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }

            // select next active productListing page
            Elements select = document.select(".PagingList .ActivePage + li a");
            if (select != null && !select.isEmpty()) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", select.attr("abs:href"),
                        downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity webPageEntity) {
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
        return "frontierfirearms.ca";
    }


}