package com.naxsoft.utils;

/**
 * Utility class to concatenate a number of parameters with separator tokens.
 * From org.apache.maven.shared.dependency.graph.internal.ItemAppender
 */
public class ItemAppender {
    private final StringBuffer buffer;

    private final String startToken;

    private final String separatorToken;

    private final String endToken;

    private boolean appended;

    public ItemAppender(StringBuffer buffer, String startToken, String separatorToken, String endToken) {
        this.buffer = buffer;
        this.startToken = startToken;
        this.separatorToken = separatorToken;
        this.endToken = endToken;

        appended = false;
    }

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
