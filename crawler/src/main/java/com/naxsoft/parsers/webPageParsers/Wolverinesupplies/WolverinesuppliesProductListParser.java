package com.naxsoft.parsers.webPageParsers.wolverinesupplies;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import com.ning.http.client.ListenableFuture;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
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
        String productDetailsJson = webPageEntity.getContent();
        Matcher itemNumberMatcher = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"").matcher(productDetailsJson);
        StringBuilder sb = new StringBuilder();

        while (itemNumberMatcher.find()) {
            sb.append(itemNumberMatcher.group(1));
            sb.append(',');
        }

        if (0 != sb.length()) {
            WebPageEntity e = new WebPageEntity();
            e.setUrl("https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetItemsData?ItemNumbersString=" + sb);
            e.setType("productPage");
            LOGGER.info("productPage={}", e.getUrl());
            result.add(e);
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
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setType("tmp");
                webPageEntity.setUrl(productDetailsUrl);
                result.add(webPageEntity);
            }
        }
        return result;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        ListenableFuture<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        Observable<WebPageEntity> tmpObservable = Observable.from(future)
                .map((document) -> parseDocument(document))
                .flatMap(Observable::from);

        return tmpObservable.flatMap(tmp -> {
            Future<WebPageEntity> result = PageDownloader.download(client, tmp);
            return Observable.from(result);
        }).flatMap(webPageEntity1 -> Observable.from(onCompleted(webPageEntity1)));
    }


    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productList");
    }
}
