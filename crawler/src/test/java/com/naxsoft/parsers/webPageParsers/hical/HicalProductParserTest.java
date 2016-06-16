package com.naxsoft.parsers.webPageParsers.hical;

import com.naxsoft.AbstractTest;
import com.naxsoft.crawler.AhcHttpClient;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;

import javax.net.ssl.SSLContext;

/**
 * Copyright NAXSoft 2015
 */
public class HicalProductParserTest extends AbstractTest {
    @Test
    public void parse() throws Exception {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
        SslContext sslContext = sslContextBuilder.build();

        try (HttpClient httpClient = new AhcHttpClient(sslContext)) {
            HicalProductParser parser = new HicalProductParser(httpClient);

            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "", false, "http://www.hical.ca/matador-sks-full-length-optic-rail-mount/", "");
            Observable<WebPageEntity> observable = parser.parse(webPageEntity);
            Iterable<WebPageEntity> webPageEntities = observable.toBlocking().toIterable();

            int counter = 0;
            for (WebPageEntity entity : webPageEntities) {
                counter++;
                Assert.assertNotNull("Expected html content", entity.getContent());
            }
            Assert.assertEquals("expected 1 product", 1, counter);
        }
    }
}