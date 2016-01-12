package com.naxsoft.parsers.webPageParsers;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser implements WebPageParser {
    /**
     * ZIP the string and return Base64 representation
     *
     * @param text
     * @return
     * @throws IOException
     */
    protected static String compress(String text) throws IOException {
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(rstBao);
        zos.write(text.getBytes());
        IOUtils.closeQuietly(zos);

        byte[] bytes = rstBao.toByteArray();
        // In my solr project, I use org.apache.solr.co mmon.util.Base64.
        // return = org.apache.solr.common.util.Base64.byteArrayToBase64(bytes, 0,
        // bytes.length);
        return Base64.getEncoder().encodeToString(bytes);

    }

}
