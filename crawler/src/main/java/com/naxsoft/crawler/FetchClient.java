//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.crawler;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Request;
import org.jsoup.Connection.Response;
import org.jsoup.helper.HttpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FetchClient {
    public Response get(String url) throws IOException {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("url=" + url);
        Connection connection = HttpConnection.connect(url);
        connection.ignoreContentType(true);
        connection.timeout((int)TimeUnit.SECONDS.toMillis(30L));
        Request request = connection.request();
        request.method(Method.GET);
        Response response = connection.execute();
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
        } catch (InterruptedException e) {
            logger.error("Thread interrupted", e);
        }
        return response;
    }
}
