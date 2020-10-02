package com.naxsoft.parsers.webPageParsers.irunguns;

import java.util.HashSet;
import java.util.Set;
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
class IrungunsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(IrungunsFrontPageParser.class);

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();

        if (document != null) {
            Elements elements = document.select("#content .widthLimit a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productPage", linkUrl, "");
                LOGGER.info("ProductPageUrl={}", linkUrl);
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity webPage) {
        return client.get("https://www.irunguns.us/product_categories.php", new DocumentCompletionHandler(webPage))
                .flatMap(this::parseDocument)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "irunguns.us";
    }

}
