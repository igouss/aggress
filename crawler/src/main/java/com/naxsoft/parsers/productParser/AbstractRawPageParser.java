package com.naxsoft.parsers.productParser;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractRawPageParser implements ProductParser {
    public static String removeNonASCII(String text) {
        return text.replaceAll("[^\\x00-\\x7F]", "");
    }
}
