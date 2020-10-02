package com.naxsoft.parsers.webPageParsers.dantesports;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
class DantesportsProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsProductListParser.class);
    private static final Pattern producePagePattern = Pattern.compile("\\d+");

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#store div.listItem");

            for (Element element : elements) {
                String onclick = element.attr("onclick");
                Matcher matcher = producePagePattern.matcher(onclick);
                if (matcher.find()) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productPage",
                            "https://shop.dantesports.com/items_detail.php?iid=" + matcher.group(), downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productPageUrl={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                } else {
                    LOGGER.info("Product id not found: {}", document.location());
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
        return "productList";
    }

    @Override
    public String getSite() {
        return "dantesports.com";
    }

}
