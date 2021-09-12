package com.naxsoft.crawler.parsers.parsers.productParser;


abstract class AbstractRawPageParser implements ProductParser {
    /**
     * @return website this parser can parse
     */
    abstract String getSite();

    /**
     * @return type of the page this parser can parse
     */
    abstract String getParserType();
}
