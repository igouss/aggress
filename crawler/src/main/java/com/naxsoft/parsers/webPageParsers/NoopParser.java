package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractWebPageParser {
    NoopParser(HttpClient client) {
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.empty();
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return false;
    }
}
