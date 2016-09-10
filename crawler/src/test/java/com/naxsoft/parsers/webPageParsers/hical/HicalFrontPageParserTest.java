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

/**
 * Copyright NAXSoft 2015
 */
public class HicalFrontPageParserTest extends AbstractTest {
    @Test
    public void parse() throws Exception {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
        SslContext sslContext = sslContextBuilder.build();
        try (HttpClient httpClient = new AhcHttpClient(sslContext)) {
            HicalFrontPageParser parser = new HicalFrontPageParser(httpClient);

            WebPageEntity webPageEntity = new WebPageEntity(null, "", "", false, "http://www.hical.ca/", "");

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