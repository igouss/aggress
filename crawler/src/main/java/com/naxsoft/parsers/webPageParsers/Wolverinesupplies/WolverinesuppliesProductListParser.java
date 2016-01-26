package com.naxsoft.parsers.webPageParsers.wolverinesupplies;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WolverinesuppliesProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesProductListParser.class);
    private final HttpClient client;

    public WolverinesuppliesProductListParser(HttpClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        Observable<WebPageEntity> tmp = Observable.create(subscriber -> client.get(parent.getUrl(), new VoidAbstractCompletionHandler2(parent, subscriber)));
        return tmp.flatMap(this::getItemsData);
    }

    private Observable<WebPageEntity> getItemsData(WebPageEntity parent) {
        return Observable.create(subscriber -> client.get(parent.getUrl(), new VoidAbstractCompletionHandler(subscriber)));
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productList");
    }

    private static class VoidAbstractCompletionHandler extends AbstractCompletionHandler<Void> {
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidAbstractCompletionHandler(Subscriber<? super WebPageEntity> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {

            String productDetailsJson = response.getResponseBody();
            Matcher itemNumberMatcher = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"").matcher(productDetailsJson);
            StringBuilder sb = new StringBuilder();

            while (itemNumberMatcher.find()) {
                sb.append(itemNumberMatcher.group(1));
                sb.append(',');
            }

            if (0 != sb.length()) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl("https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetItemsData?ItemNumbersString=" + sb);
                webPageEntity.setType("productPage");
                LOGGER.info("productPage={}", webPageEntity.getUrl());
                subscriber.onNext(webPageEntity);
            }
            subscriber.onCompleted();
            return null;
        }
    }

    private static class VoidAbstractCompletionHandler2 extends AbstractCompletionHandler<Void> {
        private final WebPageEntity parent;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidAbstractCompletionHandler2(WebPageEntity parent, Subscriber<? super WebPageEntity> subscriber) {
            this.parent = parent;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {

                Document document = Jsoup.parse(response.getResponseBody(), parent.getUrl());
                parseDocument(document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
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
                    webPageEntity.setCategory(parent.getCategory());
                    subscriber.onNext(webPageEntity);
                }
            }
        }
    }
}
