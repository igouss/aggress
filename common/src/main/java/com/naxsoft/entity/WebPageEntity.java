package com.naxsoft.entity;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Web page that can either be leaf (with produce data) or be used to find subpages
 */
@Value
@Slf4j
public class WebPageEntity {
    WebPageEntity parent;
    String content;
    String type;
    String url;
    String category;

//    /**
//     * Remove all non-ascii values from text
//     *
//     * @param text Value to sanitize
//     * @return String with only ascii values present.
//     */
//    private static String removeNonASCII(String text) {
//        return text.replaceAll("[^\\x00-\\x7F]", "");
//    }

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
}
