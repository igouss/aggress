package com.naxsoft.entity;

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web page that can either be leaf (with produce data) or be used to find subpages
 */
public class WebPageEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPageEntity.class);
    private final URL url;
    private final String type;
    private final String category;
    private final transient WebPageEntity parent;

    public WebPageEntity(WebPageEntity parent, String type, URL url, String category) {
        try {
            this.parent = parent;
            this.type = type;
            this.url = url;
            this.category = category;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate", e);
        }
    }

    public String getType() {
        return this.type;
    }

    public URL getUrl() {
        return this.url;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (null == o || getClass() != o.getClass())
            return false;

        WebPageEntity that = (WebPageEntity) o;

        return type.equals(that.type) && url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override public String toString() {
        return "WebPageEntity{" +
                "url=" + url +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", parent=" + parent +
                '}';
    }

    public WebPageEntity getParent() {
        return parent;
    }
}
