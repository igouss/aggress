package com.naxsoft.parsers.webPageParsers.marstar;

import java.util.HashSet;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class MarstarFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarFrontPageParser.class);

    public MarstarFrontPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    private static WebPageEntity create(WebPageEntity parent, URL url, String category) {
        return new WebPageEntity(parent, "productList", url, category);
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.marstar.ca/dynamic/category.jsp?catid=1", "firearm")); // firearms
        webPageEntities.add(create(parent, "http://www.marstar.ca/dynamic/category.jsp?catid=3", "ammo")); // ammo
        webPageEntities.add(create(parent, "http://www.marstar.ca/dynamic/category.jsp?catid=81526", "firearm")); // Firearms

        return Observable.from(webPageEntities)
                .observeOn(Schedulers.io())
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "marstar.ca";
    }

}

