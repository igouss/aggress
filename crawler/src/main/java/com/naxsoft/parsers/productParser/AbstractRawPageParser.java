package com.naxsoft.parsers.productParser;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractRawPageParser implements ProductParser {
    public static String removeNonASCII(String text) {
        return text.replaceAll("[^\\x00-\\x7F]", "");
    }
}
