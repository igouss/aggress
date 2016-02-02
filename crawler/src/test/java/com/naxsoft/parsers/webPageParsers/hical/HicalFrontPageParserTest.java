package com.naxsoft.parsers.webPageParsers.hical;

import com.naxsoft.AbstractTest;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.crawler.HttpClientImpl;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.util.SslUtils;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.NoSuchAlgorithmException;

/**
 * Copyright NAXSoft 2015
 */
public class HicalFrontPageParserTest extends AbstractTest {
    @Test
    public void parse() throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        try (HttpClient httpClient = new HttpClientImpl(sc)) {
            HicalFrontPageParser parser = new HicalFrontPageParser(httpClient);

            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl("http://www.hical.ca/");

            Observable<WebPageEntity> observable = parser.parse(webPageEntity);
            Iterable<WebPageEntity> webPageEntities = observable.toBlocking().toIterable();

            int counter = 0;
            for (WebPageEntity entity : webPageEntities) {
                counter++;
            }
            Assert.assertEquals(77, counter);
        }
    }
}