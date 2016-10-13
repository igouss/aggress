package com.naxsoft.utils;

import com.naxsoft.entity.WebPageEntity;

public class SitesUtil {
    public final static String[] SOURCES = {
            "http://www.alflahertys.com/",
            "http://www.bullseyelondon.com/",
            "http://www.cabelas.ca/",
            "https://www.canadaammo.com/",
            "http://www.canadiangunnutz.com/",
            "https://www.corwin-arms.com/",
            "http://www.crafm.com/",
            "http://ctcsupplies.ca/",
            "https://shop.dantesports.com/",
            "https://ellwoodepps.com/",
            "http://www.firearmsoutletcanada.com/",
            "https://fishingworld.ca/",
            "http://frontierfirearms.ca/",
            "http://gotenda.com/",
            "http://gun-shop.ca/",
            "http://www.hical.ca/",
            "http://internationalshootingsupplies.com/",
            "https://www.irunguns.us/",
            "http://www.leverarms.com/",
            "http://www.magnumguns.ca/",
            "http://www.marstar.ca/",
            "http://store.prophetriver.com/",
            "http://psmilitaria.50megs.com/",
            "https://shopquestar.com/",
            "http://www.sail.ca/",
            "http://www.theammosource.com/",
            "https://www.tradeexcanada.com/",
            "http://westrifle.com/",
            "http://wolverinegt.ca/",
            "http://www.wholesalesports.com/",
            "https://www.wolverinesupplies.com/",
    };

    public static String getHost(WebPageEntity webPageEntity) {
        String url = webPageEntity.getUrl();
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
        }
        return host;
    }
}
