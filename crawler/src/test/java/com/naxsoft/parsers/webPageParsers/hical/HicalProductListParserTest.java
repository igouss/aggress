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
public class HicalProductListParserTest extends AbstractTest {
    @Test
    public void parse() throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        try (HttpClient httpClient = new AhcHttpClient(sc)) {
            HicalProductListParser parser = new HicalProductListParser(httpClient);

            WebPageEntity webPageEntity = new WebPageEntity(0L, "", "", false, "http://www.hical.ca/sks-rifle/", "");

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