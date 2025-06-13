package com.naxsoft.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Utility class to concatenate a number of parameters with separator tokens.
 * Provides a fluent API for building delimited strings with start/end tokens.
 * From org.apache.maven.shared.dependency.graph.internal.ItemAppender
 */
@RequiredArgsConstructor
public class ItemAppender {
    @NonNull
    private final StringBuffer buffer;

    @NonNull
    private final String startToken;

    @NonNull
    private final String separatorToken;

    @NonNull
    private final String endToken;

    private boolean appended = false;

    public ItemAppender append(String item1, String item2) {
        appendToken();

        buffer.append(item1).append(item2);

        return this;
    }

    public void flush() {
        if (appended) {
            buffer.append(endToken);

            appended = false;
        }
    }

    private void appendToken() {
        buffer.append(appended ? separatorToken : startToken);

        appended = true;
    }

}
