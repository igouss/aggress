package com.naxsoft.parsers.webPageParsers.gotenda;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.reactivex.Flowable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyright NAXSoft 2015
 */
class GotendaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GotendaFrontPageParser.class);

    public GotendaFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();
        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".HorizontalDisplay> li.NavigationElement > a");
            for (Element e : elements) {
                String url = e.attr("abs:href") + "&PageSize=60&Page=1";
                WebPageEntity webPageEntity = create(downloadResult.getSourcePage(), url, e.text());
                result.add(webPageEntity);
            }
        }
        return result.build();
    }

    private Set<WebPageEntity> parseSubPages(DownloadResult downloadResult) {
        ImmutableSet.Builder<WebPageEntity> result = ImmutableSet.builder();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".InfoArea h3 a");

            List<WebPageEntity> webPageEntities = elements.stream()
                    .map(e -> create(downloadResult.getSourcePage(), e.attr("abs:href") + "&PageSize=60&Page=1", downloadResult.getSourcePage().getCategory()))
                    .collect(Collectors.toList());

            result.addAll(webPageEntities);
        }
        return result.build();
    }

    @Override
    public Flowable<WebPageEntity> parse(WebPageEntity parent) {
        return client.get(parent.getUrl(), new DocumentCompletionHandler(parent))
                .flatMapIterable(this::parseFrontPage)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMapIterable(this::parseSubPages)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "gotenda.com";
    }


}