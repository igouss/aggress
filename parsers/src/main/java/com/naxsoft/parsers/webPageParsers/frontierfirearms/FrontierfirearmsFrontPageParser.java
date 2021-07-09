package com.naxsoft.parsers.webPageParsers.frontierfirearms;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DocumentCompletionHandler;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
class FrontierfirearmsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(2);

        Document document = downloadResult.getDocument();
        if (document != null) {
            if (document.select("#CategoryPagingBottom > div > a").isEmpty()) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                log.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            } else {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                log.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);

                // select next active
                Elements select = document.select(".PagingList .ActivePage + li a");
                if (!select.isEmpty()) {
                    webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", select.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                    log.info("Product page listing={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://frontierfirearms.ca/firearms.html", "firearm"));
        webPageEntities.add(create(parent, "http://frontierfirearms.ca/ammunition-reloading.html", "ammo"));
        webPageEntities.add(create(parent, "http://frontierfirearms.ca/shooting-accessories.html", "misc"));
        webPageEntities.add(create(parent, "http://frontierfirearms.ca/optics.html", "optic"));
        return Observable.from(webPageEntities)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .filter(downloadResult -> {
                    if (downloadResult == null) {
                        log.error("Failed to get download results");
                        return false;
                    }
                    return true;
                })
                .map(this::parseDocument)
                .flatMap(Observable::from).toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "frontierfirearms.ca";
    }


}