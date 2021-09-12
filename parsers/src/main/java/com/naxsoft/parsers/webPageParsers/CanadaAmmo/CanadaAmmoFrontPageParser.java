package com.naxsoft.parsers.webPageParsers.CanadaAmmo;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
class CanadaAmmoFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    private Set<WebPageEntity> parseCategories(DownloadResult downloadResult) {
        HashSet<WebPageEntity> result = new HashSet<>();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("ul#menu-main-menu:not(.off-canvas-list) > li > a");
            log.info("Parsing for sub-pages + {}", document.location());

            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "tmp", el.attr("abs:href") + "?count=72", el.text());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    private Set<WebPageEntity> parseCategoryPages(DownloadResult downloadResult) {
        HashSet<WebPageEntity> result = new HashSet<>();

        Document document = downloadResult.getDocument();
        if (document != null) {

            Elements elements = document.select("div.clearfix span.pagination a.nav-page");
            if (elements.isEmpty()) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                log.info("productList={}, parent={}", webPageEntity.getUrl(), document.location());
                result.add(webPageEntity);
            } else {
                int i = Integer.parseInt(elements.first().text()) - 1;
                int end = Integer.parseInt(elements.last().text());
                for (; i <= end; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "&page=" + i, downloadResult.getSourcePage().getCategory());
                    log.info("productList={}, parent={}", webPageEntity.getUrl(), document.location());
                    result.add(webPageEntity);
                }
            }
        }
        return result;
    }


    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return null;
//        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
//                .map(this::parseCategories)
//                .flatMap(Observable::from)
//                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
//                .flatMap(Observable::from)
//                .map(this::parseCategoryPages)
//                .flatMap(Observable::from)
//                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "canadaammo.com";
    }

}
