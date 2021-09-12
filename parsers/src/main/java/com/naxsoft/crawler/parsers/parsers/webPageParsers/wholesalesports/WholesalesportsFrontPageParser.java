package com.naxsoft.crawler.parsers.parsers.webPageParsers.wholesalesports;

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

@Slf4j
@RequiredArgsConstructor
class WholesalesportsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            int max = 1;
            Elements elements = document.select(".pagination a");
            for (Element el : elements) {
                try {
                    int num = Integer.parseInt(el.text());
                    if (num > max) {
                        max = num;
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }

            for (int i = 0; i < max; i++) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "&page=" + i, downloadResult.getSourcePage().getCategory());
                log.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearms/c/firearms?viewPageSize=72", "firearm"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Reloading/c/reloading?viewPageSize=72", "reload"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Ammunition/c/ammunition?viewPageSize=72", "ammi"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-and-Ammunition-Storage/c/Firearm%20and%20Ammunition%20Storage?viewPageSize=72", "misc"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Optics/c/optics?viewPageSize=72", "optic"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Firearm-Accessories/c/firearm-accessories?viewPageSize=72", "misc"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Range-Accessories/c/range-accessories?viewPageSize=72", "misc"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Hunting-Accessories/c/hunting-accessories?viewPageSize=72", "misc"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Air-Guns-%26-Slingshots/c/air-guns-slingshots?viewPageSize=72", "firearm"));
        webPageEntities.add(create(parent, "http://www.wholesalesports.com/store/wsoo/en/Categories/Hunting/Black-Powder/c/black-powder?viewPageSize=72", "firearm"));

//        return Observable.from(webPageEntities)
//                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
//                .flatMap(Observable::from)
//                .map(this::parseDocument)
//                .flatMap(Observable::from).toList().toBlocking().single();
        return null;
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "wholesalesports.com";
    }
}

