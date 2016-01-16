//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

/**
 *
 */
public class NoopParser extends AbstractWebPageParser {
    public NoopParser(HttpClient client) {
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.empty();
    }

    public boolean canParse(WebPageEntity webPage) {
        return false;
    }
}
