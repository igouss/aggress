package com.naxsoft.utils

import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

public object Compressor {
    /**
     * ZIP the string and return Base64 representation

     * @param text Value to compress
     * *
     * @return Compressed Value
     */
    public fun compress(text: String): String {
        if (text.isEmpty()) {
            return text
        }

        val rstBao = ByteArrayOutputStream()
        val zos: GZIPOutputStream
        zos = GZIPOutputStream(rstBao)
        zos.write(text.toByteArray())
        IOUtils.closeQuietly(zos)

        val bytes = rstBao.toByteArray()
        // In my solr project, I use org.apache.solr.co mmon.util.Base64.
        // return = org.apache.solr.common.util.Base64.byteArrayToBase64(bytes, 0,
        // bytes.length);
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * Unzip a BASE64 string

     * @param zippedBase64Str value to decompress
     * *
     * @return Decompresed value
     * *
     * @throws IOException in case of decompression error
     */
    @Throws(IOException::class)
    public fun decompress(zippedBase64Str: String): String {
        if (zippedBase64Str.isEmpty()) {
            return zippedBase64Str
        }
        val bytes = Base64.getDecoder().decode(zippedBase64Str)
        var zi: GZIPInputStream? = null
        try {
            zi = GZIPInputStream(ByteArrayInputStream(bytes))
            return IOUtils.toString(zi, Charset.forName("UTF-8"))
        } finally {
            IOUtils.closeQuietly(zi)
        }
    }
}