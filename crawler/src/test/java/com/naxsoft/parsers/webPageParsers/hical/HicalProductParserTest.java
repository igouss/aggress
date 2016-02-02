package com.naxsoft.parsers.webPageParsers.hical;

import com.naxsoft.AbstractTest;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.crawler.HttpClientImpl;
import com.naxsoft.entity.WebPageEntity;
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
        SSLContext sc = SSLContext.getInstance("SSL");
        try (HttpClient httpClient = new HttpClientImpl(sc)) {
            HicalProductParser parser = new HicalProductParser(httpClient);

            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl("http://www.hical.ca/matador-sks-full-length-optic-rail-mount/");

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