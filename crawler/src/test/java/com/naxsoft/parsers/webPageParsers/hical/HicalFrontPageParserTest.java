package com.naxsoft.parsers.webPageParsers.hical;

import com.naxsoft.AbstractTest;
import com.naxsoft.crawler.AhcHttpClient;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;

import javax.net.ssl.SSLContext;

/**
 * Copyright NAXSoft 2015
 */
public class HicalFrontPageParserTest extends AbstractTest {
    @Test
    public void parse() throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        try (HttpClient httpClient = new AhcHttpClient(sc)) {
            HicalFrontPageParser parser = new HicalFrontPageParser(httpClient);

            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "", false, "http://www.hical.ca/", "");

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