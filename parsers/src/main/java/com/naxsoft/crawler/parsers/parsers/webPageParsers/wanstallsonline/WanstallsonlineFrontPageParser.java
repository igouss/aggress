package com.naxsoft.crawler.parsers.parsers.webPageParsers.wanstallsonline;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
class WanstallsonlineFrontPageParser extends AbstractWebPageParser {
    private static final Pattern pageNumPattern = Pattern.compile("\\d+");
    private final HttpClient client;

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();

        if (document != null) {
            int max = 1;
            Elements elements = document.select(".navigationtable td[valign=middle] > a");
            for (Element el : elements) {
                try {
                    Matcher matcher = pageNumPattern.matcher(el.text());
                    if (matcher.find()) {
                        int num = Integer.parseInt(matcher.group());
                        if (num > max) {
                            max = num;
                        }
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }

            for (int i = 1; i < max; i++) {
                String url;
                if (1 == i) {
                    url = document.location();
                } else {
                    url = document.location() + "index " + i + ".html";
                }
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", url, downloadResult.getSourcePage().getCategory());
                log.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/firearms/", "firearm"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/optics/", "optic"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/tactical-accessories/", "misc"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/gun-cleaning/", "misc"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/storage-transport/", "misc"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/hunting-shooting-supplies/", "misc"));
        webPageEntities.add(create(parent, "http://www.wanstallsonline.com/firearms-ammunition", "ammo"));
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
        return "wanstallsonline.com";
    }
}

