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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class FetchClient {
    public Response get(String url) throws IOException {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("GET " + url);
        Connection connection = HttpConnection.connect(url);
        connection.ignoreContentType(true);
        connection.timeout((int) TimeUnit.SECONDS.toMillis(60L));
        Request request = connection.request();
        request.method(Method.GET);
        Response response = connection.execute();
//        try {
//            Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
//        } catch (InterruptedException e) {
//            logger.error("Thread interrupted", e);
//        }
        return response;
    }

    public String put(String url, String content) throws IOException {
        String result = "";
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("PUT " + url);
        try {
            URL addr = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) addr.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            os.write(content.getBytes());
            os.flush();

//            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
//                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
//            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            while ((output = br.readLine()) != null) {
                result += output;
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
