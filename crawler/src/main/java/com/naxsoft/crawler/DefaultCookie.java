package com.naxsoft.crawler;

import org.asynchttpclient.cookie.Cookie;

public class DefaultCookie extends Cookie {
    public DefaultCookie(String name, String value) {
        super(name, value, false, null,null, 0, false, false);
    }
    public DefaultCookie(String name, String value, boolean wrap, String domain, String path, long maxAge, boolean secure, boolean httpOnly) {
        super(name, value, wrap, domain, path, maxAge, secure, httpOnly);
    }
}
