package com.naxsoft.parsers.webPageParsers.Wolverinesupplies;

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
public class WolverinesuppliesProductListParser extends AbstractWebPageParser {
    private static final Pattern itemNumberPattern = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"");
    private static final Pattern categoryPattern = Pattern.compile("'\\d+'");

    private final HttpClient client;

    private Set<WebPageEntity> onCompleted(WebPageEntity parent) {
        Set<WebPageEntity> result = new HashSet<>();
        try {
            String productDetailsJson = parent.getContent();
            Matcher itemNumberMatcher = itemNumberPattern.matcher(productDetailsJson);
            StringBuilder sb = new StringBuilder();

            while (itemNumberMatcher.find()) {
                String item = itemNumberMatcher.group(1);
                sb.append(item);
                sb.append(',');
            }

            if (0 != sb.length()) {
                WebPageEntity e = new WebPageEntity(parent, "", "productPage", "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetItemsData?ItemNumbersString=" + sb, parent.getCategory());
                log.info("productPage={}", e.getUrl());
                result.add(e);
            }
        } catch (NullPointerException npe) {
            log.error("NPE = {}", parent, npe);
        }
        return result;
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("div[ng-init]");

            for (Element e : elements) {
                String linkUrl = e.attr("ng-init");
                Matcher categoryMatcher = categoryPattern.matcher(linkUrl);

                if (categoryMatcher.find()) {
                    String productCategory = categoryMatcher.group();
                    String productDetailsUrl = "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetJSONItems?data={\"WordList\":\"\",\"ItemNumber\":\"\",\"CategoryCode\":" + productCategory + ",\"SearchMethod\":\"Category\",\"Limit\":0}";
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "tmp", productDetailsUrl, downloadResult.getSourcePage().getCategory());
                    result.add(webPageEntity);
                }
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
//        return Observable.from(client.get(webPageEntity.getUrl() + "?sortValue=0&Stock=In%20Stock", new DocumentCompletionHandler(webPageEntity)))
//                .map(this::parseDocument)
//                .flatMap(Observable::from)
//                .map(page -> PageDownloader.download(client, page, "tmp"))
//                .flatMap(Observable::from)
//                .map(this::onCompleted)
//                .flatMap(Observable::from).toList().toBlocking().single();
        return null;
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "wolverinesupplies.com";
    }
}
