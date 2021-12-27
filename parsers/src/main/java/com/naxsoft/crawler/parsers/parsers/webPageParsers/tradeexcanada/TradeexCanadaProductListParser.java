package com.naxsoft.crawler.parsers.parsers.webPageParsers.tradeexcanada;

import com.naxsoft.common.entity.WebPageEntity;
import com.naxsoft.crawler.parsers.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
class TradeexCanadaProductListParser extends AbstractWebPageParser {
    private final HttpClient client;

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            if (document.location().contains("page=")) {
                Elements elements = document.select(".view-content a");
                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                    log.info("productPageUrl={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            } else {
                Elements subPages = document.select(".pager a");
                List<WebPageEntity> webPageEntities = subPages.stream()
                        .map(subPage -> create(downloadResult.getSourcePage(), subPage.attr("abs:href"), downloadResult.getSourcePage().getCategory()))
                        .collect(Collectors.toList());
                result.addAll(webPageEntities);
                result.add(create(downloadResult.getSourcePage(), document.location() + "?page=0", downloadResult.getSourcePage().getCategory()));
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
//        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
//                .map(this::parseDocument)
//                .flatMap(Observable::from).toList().toBlocking().single();
        return null;
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "tradeexcanada.com";
    }
}