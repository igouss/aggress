//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers.wolverinesupplies;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;

public class WolverinesuppliesProductPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(WolverinesuppliesProductPageParser.class);

    public WolverinesuppliesProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return PageDownloader.download(client, webPage.getUrl());
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productPage");
    }
}
