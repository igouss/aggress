package com.naxsoft.parsers.webPageParsers.Wolverinesupplies;

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

public class WolverinesuppliesFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesFrontPageParser.class);

    public WolverinesuppliesFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".mainnav a");
            for (Element e : elements) {
                if (e.hasClass("sfSel") || e.text().equals("Range Events")) {
                    continue;
                }
                String linkUrl = e.attr("abs:href");
                if (null != linkUrl && !linkUrl.isEmpty() && linkUrl.contains("Products") && e.siblingElements().isEmpty()) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", linkUrl, e.text());
                    LOGGER.info("ProductPageUrl={}", linkUrl);
                    result.add(webPageEntity);
                }
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
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "wolverinesupplies.com";
    }


}
