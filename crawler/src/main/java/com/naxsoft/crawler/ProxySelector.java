package com.naxsoft.crawler;

import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.proxy.ProxyServerSelector;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class ProxySelector implements ProxyServerSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxySelector.class);
    private ProxyServer proxyServer = null;

    ProxySelector() {

    }

    @Override
    public ProxyServer select(Uri uri) {
        return proxyServer;
    }
}