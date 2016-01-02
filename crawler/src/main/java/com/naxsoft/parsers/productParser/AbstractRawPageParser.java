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

    /**
     * Unzip a BASE64 string
     *
     * @param zippedBase64Str
     * @return
     * @throws IOException
     */
    protected static String fromZip(String zippedBase64Str) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(zippedBase64Str);
        GZIPInputStream zi = null;
        try {
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            return IOUtils.toString(zi);
        } finally {
            IOUtils.closeQuietly(zi);
        }
    }
}
