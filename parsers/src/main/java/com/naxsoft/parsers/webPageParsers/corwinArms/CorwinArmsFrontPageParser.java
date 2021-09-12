package com.naxsoft.parsers.webPageParsers.corwinArms;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
class CorwinArmsFrontPageParser extends AbstractWebPageParser {
    private static final Pattern pagerPattern = Pattern.compile("(\\d+) of (\\d+)");
    private final HttpClient client;

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#block-menu-menu-catalogue > div > ul a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");

                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", linkUrl, e.text());
                log.info("Found productList page {}", linkUrl);
                result.add(webPageEntity);
            }
        }
        return result;
    }

    private Set<WebPageEntity> parseDocument2(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".pager li.pager-current");
            Matcher matcher = pagerPattern.matcher(elements.text());
            if (matcher.find()) {
                int max = Integer.parseInt(matcher.group(2));
                for (int i = 1; i <= max; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "?page=" + i, downloadResult.getSourcePage().getCategory());
                    log.info("Product page listing={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            } else {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                log.info("Product page listing={}", webPageEntity.getUrl());
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
//                .flatMap(Observable::from)
//                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
//                .flatMap(Observable::from)
//                .map(this::parseDocument2)
//                .flatMap(Observable::from)
//                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "corwin-arms.com";
    }


}
