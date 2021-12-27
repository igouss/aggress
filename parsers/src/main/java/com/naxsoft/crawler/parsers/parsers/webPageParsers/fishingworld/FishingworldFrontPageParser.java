package com.naxsoft.crawler.parsers.parsers.webPageParsers.fishingworld;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
class FishingworldFrontPageParser extends AbstractWebPageParser {
    private static final Pattern maxPagesPattern = Pattern.compile("(\\d+) of (\\d+)");
    private final HttpClient client;

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#list > div.bar.blue");
            Matcher matcher = maxPagesPattern.matcher(elements.text());
            if (matcher.find()) {
                int max = Integer.parseInt(matcher.group(2));
                int postsPerPage = 10;
                int pages = (int) Math.ceil((double) max / postsPerPage);

                for (int i = 1; i <= pages; i++) {
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
    public List<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/66-guns", "firearm"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/67-ammunition", "ammo"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/66-guns", "firearm"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/146-optics", "optic"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/144-shooting-accesories", "misc"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/185-tree-stands", "misc"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/65-accessories", "misc"));
        webPageEntities.add(create(parent, "https://fishingworld.ca/hunting/205-pellet-gun", "firearm"));

        return null;

//        return Observable.from(webPageEntities)
//                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
//                .flatMap(Observable::from)
//                .map(this::parseDocument)
//                .flatMap(Observable::from)
//                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "fishingworld.ca";
    }
}