package com.naxsoft.parsers.webPageParsers.Wolverinesupplies;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import io.vertx.core.eventbus.Message;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WolverinesuppliesProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesProductListParser.class);
    private static final Pattern itemNumberPattern = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"");
    private static final Pattern categoyPattern = Pattern.compile("\'\\d+\'");
    private final HttpClient client;

    public WolverinesuppliesProductListParser(HttpClient client) {
        this.client = client;
    }

    private Set<WebPageEntity> onCompleted(WebPageEntity webPageEntity) {
        Set<WebPageEntity> result = new HashSet<>();
        try {
            String productDetailsJson = webPageEntity.getContent();
            Matcher itemNumberMatcher = itemNumberPattern.matcher(productDetailsJson);
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
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("div[ng-init]");

            for (Element e : elements) {
                String linkUrl = e.attr("ng-init");
                Matcher categoryMatcher = categoyPattern.matcher(linkUrl);

                if (categoryMatcher.find()) {
                    String productCategory = categoryMatcher.group();
                    String productDetailsUrl = "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetJSONItems?data={\"WordList\":\"\",\"ItemNumber\":\"\",\"CategoryCode\":" + productCategory + ",\"SearchMethod\":\"Category\",\"Limit\":0}";
                    WebPageEntity webPageEntity = new WebPageEntity(0L, "", "tmp", false, productDetailsUrl, downloadResult.getSourcePage().getCategory());
                    result.add(webPageEntity);
                }
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return Observable.from(client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)), Schedulers.io())
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .map(webPageEntity1 -> PageDownloader.download(client, webPageEntity1))
                .flatMap(Observable::from)
                .filter(data -> null != data)
                .map(this::onCompleted)
                .flatMap(Observable::from);
    }


    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("wolverinesupplies.com") && webPage.getType().equals("productList");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("wolverinesupplies.com/productList", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
