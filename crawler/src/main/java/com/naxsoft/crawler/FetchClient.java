package com.naxsoft.crawler;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Copyright NAXSoft 2015
 */
public class FetchClient {

    public Connection.Response get(String url) throws IOException {
        Logger logger = LoggerFactory.getLogger(FetchClient.class);
        logger.info("url=" +url);
        Connection connection = HttpConnection.connect(url);
        Connection.Request request = connection.request();
        request.method(Connection.Method.GET);
        Connection.Response response = connection.execute();
        return response;
    }
}
