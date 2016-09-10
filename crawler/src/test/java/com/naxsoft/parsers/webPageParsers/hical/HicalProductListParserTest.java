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
public class HicalProductListParserTest extends AbstractTest {
    @Test
    public void parse() throws Exception {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
        SslContext sslContext = sslContextBuilder.build();

        try (HttpClient httpClient = new AhcHttpClient(sslContext)) {
            HicalProductListParser parser = new HicalProductListParser(httpClient);

            WebPageEntity webPageEntity = new WebPageEntity(null, "", "", false, "http://www.hical.ca/sks-rifle/", "");

            Observable<WebPageEntity> observable = parser.parse(webPageEntity);
            Iterable<WebPageEntity> webPageEntities = observable.toBlocking().toIterable();

            int counter = 0;
            int subCategoryCounter = 0;

            for (WebPageEntity entity : webPageEntities) {

                if (entity.getType().equals("productPage")) {
                    counter++;
                } else if (entity.getType().equals("productList")) {
                    subCategoryCounter++;
                }
            }
            Assert.assertEquals(20, counter);
            Assert.assertEquals(2, subCategoryCounter);
        }
    }
}