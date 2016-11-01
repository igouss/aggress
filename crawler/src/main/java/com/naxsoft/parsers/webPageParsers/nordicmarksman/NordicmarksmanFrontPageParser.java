package com.naxsoft.parsers.webPageParsers.nordicmarksman;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class NordicmarksmanFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NordicmarksmanFrontPageParser.class);

    public NordicmarksmanFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Observable<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("div.menuwrapper > ul > li:nth-child(1)  a");
            List<WebPageEntity> webPageEntities = elements.stream()
                    .filter(e -> e.attr("abs:href").endsWith(".html"))
                    .map(e -> create(downloadResult.getSourcePage(), e.attr("abs:href") + "?show=all", e.text()))
                    .collect(Collectors.toList());
            result.addAll(webPageEntities);
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return client.get("http://www.nordicmarksman.com/", new DocumentCompletionHandler(parent))
                .flatMap(this::parseFrontPage)
                .doOnNext(e -> this.parseResultCounter.inc());

    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "nordicmarksman.com";
    }

}