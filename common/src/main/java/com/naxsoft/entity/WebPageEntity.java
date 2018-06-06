package com.naxsoft.entity;

import com.naxsoft.utils.Compressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Web page that can either be leaf (with produce data) or be used to find subpages
 */
public class WebPageEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPageEntity.class);

    private String url;
    private String type;
    private String content;
    private String category;

    private transient WebPageEntity parent;

    public WebPageEntity(WebPageEntity parent, String content, String type, String url, String category) {
        this.parent = parent;
        try {
            this.content = Compressor.compress(removeNonASCII(content));
        } catch (IOException e) {
            this.content = removeNonASCII(content);
        }
        this.type = type;
        this.url = url;
        this.category = category;
    }


    /**
     * Remove all non-ascii values from text
     *
     * @param text Value to sanitize
     * @return String with only ascii values present.
     */
    private static String removeNonASCII(String text) {
        return text.replaceAll("[^\\x00-\\x7F]", "");
    }

    /**
     * Get pages's HTML
     */
    public String getContent() {
        String result = "";

        if (null != this.content) {
            try {
                result = Compressor.decompress(this.content);
            } catch (IOException e) {
                LOGGER.error("Failed to decompress", e);
            }
        }
        return result;
    }

    public String getType() {
        return this.type;
    }

    public String getUrl() {
        return this.url;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        WebPageEntity that = (WebPageEntity) o;

        return type.equals(that.type) && url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "WebPageEntity{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    public WebPageEntity getParent() {
        return parent;
    }
}
