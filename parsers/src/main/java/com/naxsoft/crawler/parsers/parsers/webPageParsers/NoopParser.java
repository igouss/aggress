package com.naxsoft.crawler.parsers.parsers.webPageParsers;

import com.naxsoft.common.entity.WebPageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class NoopParser extends AbstractWebPageParser {
    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        List<WebPageEntity> result = new ArrayList<>(0);
        log.error("Using NOOP parser for: " + webPage);
        return result;
    }

    @Override
    public String getParserType() {
        return "noopWebPageParser";
    }

    @Override
    public String getSite() {
        return "anySite";
    }
}
