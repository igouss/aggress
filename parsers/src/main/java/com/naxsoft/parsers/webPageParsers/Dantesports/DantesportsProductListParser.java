package com.naxsoft.parsers.webPageParsers.Dantesports;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DocumentCompletionHandler;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
class DantesportsProductListParser extends AbstractWebPageParser {
    private static final Pattern producePagePattern = Pattern.compile("\\d+");
    private final HttpClient client;

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#store div.listItem");

            for (Element element : elements) {
                String onclick = element.attr("onclick");
                Matcher matcher = producePagePattern.matcher(onclick);
                if (matcher.find()) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", "https://shop.dantesports.com/items_detail.php?iid=" + matcher.group(), downloadResult.getSourcePage().getCategory());
                    log.info("productPageUrl={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                } else {
                    log.info("Product id not found: {}", document.location());
                }
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .toList().toBlocking().single();
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
