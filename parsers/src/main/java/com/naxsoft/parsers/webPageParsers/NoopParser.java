package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class NoopParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopParser.class);

    public NoopParser(HttpClient client) {
        super(client);
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        List<WebPageEntity> result = new ArrayList<>(0);
        LOGGER.error("Using NOOP parser for: " + webPage);
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
