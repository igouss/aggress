package com.naxsoft.parsers.webPageParsers.prophetriver;

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
import rx.Observable;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class ProphetriverProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProphetriverProductListParser.class);

    public ProphetriverProductListParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".ProductLink a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", element.attr("abs:href"), null);
                LOGGER.info("productPageUrl={}, parent={}", webPageEntity.getUrl(), downloadResult.getSourcePage().getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
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
        return "prophetriver.com";
    }

}