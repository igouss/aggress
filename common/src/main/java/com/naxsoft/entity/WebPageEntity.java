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

    private transient WebPageEntity parent;
    private String content;
    private String type;
    private String url;
    private String category;

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

    public WebPageEntity getParent() {
        return parent;
    }

    public String getHost() {
        String host = "noopWebPageParser";
        if (url.contains("alflahertys.com")) {
            host = "alflahertys.com";
        } else if (url.contains("bullseyelondon.com")) {
            host = "bullseyelondon.com";
        } else if (url.contains("cabelas.ca")) {
            host = "cabelas.ca";
        } else if (url.contains("canadaammo.com")) {
            host = "canadaammo.com";
        } else if (url.contains("canadiangunnutz.com")) {
            host = "canadiangunnutz.com";
        } else if (url.contains("corwin-arms.com")) {
            host = "corwin-arms.com";
        } else if (url.contains("crafm.com")) {
            host = "crafm.com";
        } else if (url.contains("ctcsupplies.ca")) {
            host = "ctcsupplies.ca";
        } else if (url.contains("ctcsupplies.ca")) {
            host = "ctcsupplies.ca";
        } else if (url.contains("dantesports.com")) {
            host = "dantesports.com";
        } else if (url.contains("ellwoodepps.com")) {
            host = "ellwoodepps.com";
        } else if (url.contains("firearmsoutletcanada.com")) {
            host = "firearmsoutletcanada.com";
        } else if (url.contains("fishingworld.ca")) {
            host = "fishingworld.ca";
        } else if (url.contains("frontierfirearms.ca")) {
            host = "frontierfirearms.ca";
        } else if (url.contains("gotenda.com")) {
            host = "gotenda.com";
        } else if (url.contains("gun-shop.ca")) {
            host = "gun-shop.ca";
        } else if (url.contains("hical.ca")) {
            host = "hical.ca";
        } else if (url.contains("internationalshootingsupplies.com")) {
            host = "internationalshootingsupplies.com";
        } else if (url.contains("irunguns.us")) {
            host = "irunguns.us";
        } else if (url.contains("leverarms.com")) {
            host = "leverarms.com";
        } else if (url.contains("magnumguns.ca")) {
            host = "magnumguns.ca";
        } else if (url.contains("marstar.ca")) {
            host = "marstar.ca";
        } else if (url.contains("prophetriver.com")) {
            host = "prophetriver.com";
        } else if (url.contains("psmilitaria.50megs.com")) {
            host = "psmilitaria.50megs.com";
        } else if (url.contains("shopquestar.com")) {
            host = "shopquestar.com";
        } else if (url.contains("sail.ca")) {
            host = "sail.ca";
        } else if (url.contains("theammosource.com")) {
            host = "theammosource.com";
        } else if (url.contains("tradeexcanada.com")) {
            host = "tradeexcanada.com";
        } else if (url.contains("wanstallsonline.com")) {
            host = "wanstallsonline.com";
        } else if (url.contains("westrifle.com")) {
            host = "westrifle.com";
        } else if (url.contains("wolverinegt.ca")) {
            host = "wolverinegt.ca";
        } else if (url.contains("wholesalesports.com")) {
            host = "wholesalesports.com";
        } else if (url.contains("wolverinesupplies.com")) {
            host = "wolverinesupplies.com";
        } else if (url.contains("nordicmarksman.com")) {
            host = "nordicmarksman.com";
        } else if (url.contains("grouseriver.com")) {
            host = "grouseriver.com";
        } else if (url.contains("westcoasthunting.ca")) {
            host = "westcoasthunting.ca";
        } else if (url.contains("ammosupply.ca")) {
            host = "ammosupply.ca";
        } else if (url.contains("gunhub.ca")) {
            host = "gunhub.ca";
        } else if (url.contains("durhamoutdoors.ca")) {
            host = "durhamoutdoors.ca";
        }
        return host;
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
}
