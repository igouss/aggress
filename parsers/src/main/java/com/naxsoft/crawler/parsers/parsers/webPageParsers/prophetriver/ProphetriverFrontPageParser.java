package com.naxsoft.crawler.parsers.parsers.webPageParsers.prophetriver;

import com.naxsoft.common.entity.WebPageEntity;
import com.naxsoft.crawler.parsers.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
class ProphetriverFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#AccordianWrapper a");
            List<WebPageEntity> webPageEntities = elements.stream()
                    .map(e -> create(downloadResult.getSourcePage(), e.attr("abs:href"), null))
                    .collect(Collectors.toList());
            result.addAll(webPageEntities);
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
//        return Observable.from(client.get("http://store.prophetriver.com/categories/", new DocumentCompletionHandler(webPageEntity)))
//                .map(this::parseFrontPage)
//                .flatMap(Observable::from).toList().toBlocking().single();
        return null;
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "prophetriver.com";
    }
}