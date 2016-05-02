package com.naxsoft.parsers.webPageParsers.Wolverinesupplies;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WolverinesuppliesProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesProductListParser.class);
    private final HttpClient client;

    public WolverinesuppliesProductListParser(HttpClient client) {
        this.client = client;
    }

    public Set<WebPageEntity> onCompleted(WebPageEntity webPageEntity) {
        Set<WebPageEntity> result = new HashSet<>();
        try {
            String productDetailsJson = webPageEntity.getContent();
            Matcher itemNumberMatcher = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"").matcher(productDetailsJson);
            StringBuilder sb = new StringBuilder();

            while (itemNumberMatcher.find()) {
                sb.append(itemNumberMatcher.group(1));
                sb.append(',');
            }

            if (0 != sb.length()) {
                WebPageEntity e = new WebPageEntity(0L, "", "productPage", false, "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetItemsData?ItemNumbersString=" + sb, webPageEntity.getCategory());
                LOGGER.info("productPage={}", e.getUrl());
                result.add(e);
            }
        } catch (NullPointerException npe) {
            LOGGER.error("NPE = {}", webPageEntity, npe);
        }
        return result;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("div[ng-init]");

        for (Element e : elements) {
            String linkUrl = e.attr("ng-init");
            Matcher categoryMatcher = Pattern.compile("\'\\d+\'").matcher(linkUrl);

            if (categoryMatcher.find()) {
                String productCategory = categoryMatcher.group();
                String productDetailsUrl = "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetJSONItems?data={\"WordList\":\"\",\"ItemNumber\":\"\",\"CategoryCode\":" + productCategory + ",\"SearchMethod\":\"Category\",\"Limit\":0}";
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "tmp", false, productDetailsUrl, downloadResult.getSourcePage().getCategory());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .map(webPageEntity1 -> PageDownloader.download(client, webPageEntity1))
                .flatMap(Observable::from)
                .filter(data -> null != data)
                .map(this::onCompleted)
                .flatMap(Observable::from);
    }


    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productList");
    }
}
