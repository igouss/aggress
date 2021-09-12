package com.naxsoft.parsers.webPageParsers.ellwoodepps;

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
class EllwoodeppsProductListParser extends AbstractWebPageParser {
    private final HttpClient client;

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".firearm-table");
            for (Element element : elements) {
                String category;
                if (downloadResult.getSourcePage().getCategory().equalsIgnoreCase("accessories")) {
                    category = element.select(".firearm-table > tbody > tr:nth-child(2) > td:nth-child(2)").text();
                } else {
                    category = downloadResult.getSourcePage().getCategory();
                }
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", element.select("td.firearm-name > a").attr("abs:href"), category);
                log.info("productPageUrl={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return null;
//        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
//                .map(this::parseDocument)
//                .flatMap(Observable::from).toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "ellwoodepps.com";
    }
}