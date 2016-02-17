package com.naxsoft.parsers.productParser;

import com.naxsoft.AbstractTest;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.crawler.HttpClientImpl;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzRawPageParserTest extends AbstractTest {
    private CanadiangunnutzRawPageParser parser = null;

    @Test
    public void parse() throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        try (HttpClient client = new HttpClientImpl(sc)) {
            CanadiangunnutzRawPageParser parser = new CanadiangunnutzRawPageParser();
            InputStream pageStream = CabelasProductRawParserTest.class.getClassLoader().getResourceAsStream("canadiangunnutzProductPage.html");

            WebPageEntity entity = new WebPageEntity();
            entity.setCategory("Firearm");
            entity.setContent(IOUtils.toString(pageStream));
            entity.setUrl("http://www.canadiangunnutz.com/forum/showthread.php/1355228-Lee-Enfield-EAL-3-digit-serial-Mint");
            Set<ProductEntity> result = parser.parse(entity);
            Assert.assertEquals(1, result.size());
            ProductEntity product = result.iterator().next();
            Assert.assertEquals("", product.getJson());
        }
    }
}