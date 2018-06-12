package com.naxsoft.parsers.webPageParsers.Dantesports;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DocumentCompletionHandler;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class DantesportsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsFrontPageParser.class);

    public DantesportsFrontPageParser(HttpClient client) {
        super(client);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#scol1 > div.scell_menu > li > a");

            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", element.attr("abs:href") + "&paging=0", element.text());
                LOGGER.info("productList={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(client.get("https://shop.dantesports.com/set_lang.php?lang=EN", new LinkedList<>(), false, getCookiesHandler()))
                .first()
                .map(cookies1 -> client.get(webPage.getUrl(), cookies1, new DocumentCompletionHandler(webPage)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "dantesports.com";
    }
}