package com.naxsoft.parsers.productParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;

/**
 * Copyright NAXSoft 2015
 */
public class ProductParserFactory {
    private final Map<String, AbstractRawPageParser> productParsers;

    public ProductParserFactory(List<AbstractRawPageParser> parsers) {
        this.productParsers = new HashMap<>();
        parsers.forEach(p -> this.productParsers.put(p.getSite() + "/" + p.getParserType(), p));
    }

    /**
     * Get ProductParser capable of parsing webPageEntity
     *
     * @param webPageEntity page to parse
     * @return Parser capable of parsing the page
     */
    public Iterable<ProductEntity> parse(WebPageEntity webPageEntity) {
        String host = webPageEntity.getUrl().getHost();
        String type = webPageEntity.getType();
        String mailbox = host + "/" + type;
        return productParsers.get(mailbox).parse(webPageEntity);
    }
}
