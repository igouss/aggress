package com.naxsoft.crawler;

import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.proxy.ProxyServerSelector;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class TorProxySelector implements ProxyServerSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorProxySelector.class);
    private ProxyServer proxyServer = null;

    TorProxySelector() {
        try {
            String proxyHost = AppProperties.getProperty("proxy.host").getValue();
            int proxyPort = Integer.parseInt(AppProperties.getProperty("proxy.port").getValue());
            proxyServer = new ProxyServer.Builder(proxyHost, proxyPort).build();
            LOGGER.info("Proxy: {}:{}", proxyServer.getHost(), proxyServer.getPort());
        } catch (PropertyNotFoundException ignore) {
            LOGGER.info("Proxy: no proxy");
        }
    }

    @Override
    public ProxyServer select(Uri uri) {
        return proxyServer;
    }
}