//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

import java.util.Set;

public class NoopParser implements WebPageParser {
    public NoopParser(AsyncFetchClient<Set<WebPageEntity>> client) {
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) {
        return Observable.empty();
    }

    public boolean canParse(WebPageEntity webPage) {
        return false;
    }
}
