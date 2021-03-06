package com.naxsoft.crawler;

import org.asynchttpclient.proxy.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

class ProxyManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyManager.class);
    private int current = 0;
    private ArrayList<ProxyServer> proxyServers = null;

    ProxyManager() {
//        List<String> nonProxyHosts = new ArrayList<>();
//
//        nonProxyHosts.add("localhost");
//        nonProxyHosts.add("elasticsearch");
//        nonProxyHosts.add("redis");
//        ProxyServer torProxy = new ProxyServer.Builder("tor-proxy", 8118)
//                .setNonProxyHosts(nonProxyHosts)
//                .build();

        proxyServers = new ArrayList<>();
//        proxyServers.add(torProxy);

//        try {
//            String proxyHost = AppProperties.getProperty("proxy.host").getValue();
//            int proxyPort = Integer.parseInt(AppProperties.getProperty("proxy.port").getValue());
//            proxyServers.add(new ProxyServer.Builder(proxyHost, proxyPort).build());
//        } catch (PropertyNotFoundException e) {
//            LOGGER.warn("unable to find proxy");

        if (proxyServers.isEmpty()) {
            LOGGER.warn("no proxy");
        }
    }

    synchronized ProxyServer getProxyServer() {
        ProxyServer result = null;
        //

        if (!proxyServers.isEmpty()) {
            result = proxyServers.get(current++);
            if (current >= proxyServers.size()) {
                current = 0;
            }
        }
        return result;
    }

    synchronized void proxyFailed(String address) {
        for (int i = 0; i < proxyServers.size(); i++) {
            if (proxyServers.get(i).getHost().equalsIgnoreCase(address)) {
                LOGGER.debug("Marking proxy {} as failed", address);
                proxyServers.remove(i);
                break;
            }
        }
    }
}
