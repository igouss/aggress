package com.naxsoft.utils;

import org.elasticsearch.common.mustache.MustacheException;

import java.io.IOException;
import java.io.Writer;

/**
 * Copyright NAXSoft 2015
 */
public class ElasticEscape {
    public static void escape(String value, Writer writer) {
        try {
            int e = 0;
            int length = value.length();

            for(int i = 0; i < length; ++i) {
                char c = value.charAt(i);

                switch(c) {
                    case '\\':
                        e = append(value, writer, e, i, "\\\\");
                        break;
                    case '/':
                        e = append(value, writer, e, i, "//");
                        break;
                }
            }

            writer.append(value, e, length);
        } catch (IOException var7) {
            throw new MustacheException("Failed to encode value: " + value);
        }
    }
    private static int append(String value, Writer writer, int position, int i, String replace) throws IOException {
        writer.append(value, position, i);
        writer.append(replace);
        return i + 1;
    }

}
