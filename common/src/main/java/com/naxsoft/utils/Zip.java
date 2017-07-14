package com.naxsoft.utils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Zip {
    /**
     * ZIP the string and return Base64 representation
     *
     * @param text Value to compress
     * @return Compressed Value
     */
    public static String compress(String text) throws IOException {
        if (text.isEmpty()) {
            return text;
        }

        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos;
            zos = new GZIPOutputStream(rstBao);
            zos.write(text.getBytes());
            IOUtils.closeQuietly(zos);

            byte[] bytes = rstBao.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Unzip a BASE64 string
     *
     * @param zippedBase64Str value to decompress
     * @return Decompresed value
     * @throws IOException in case of decompression error
     */
    public static String decompress(String zippedBase64Str) throws IOException {
        if (zippedBase64Str.isEmpty()) {
            return zippedBase64Str;
        }
        byte[] bytes = Base64.getDecoder().decode(zippedBase64Str);
        GZIPInputStream zi = null;
        try {
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            return IOUtils.toString(zi, Charset.forName("UTF-8"));
        } finally {
            IOUtils.closeQuietly(zi);
        }
    }
}
