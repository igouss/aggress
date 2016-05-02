package com.naxsoft.parsers.productParser;

import com.naxsoft.AbstractTest;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzRawPageParserTest extends AbstractTest {
    private CanadiangunnutzRawPageParser parser = null;

    @Test
    public void parse() throws Exception {
        InputStream pageStream = CabelasProductRawParserTest.class.getClassLoader().getResourceAsStream("canadiangunnutzProductPage.html");
        try {
            CanadiangunnutzRawPageParser parser = new CanadiangunnutzRawPageParser();
            WebPageEntity entity = new WebPageEntity(0L, IOUtils.toString(pageStream), "", false, "http://www.canadiangunnutz.com/forum/showthread.php/1355228-Lee-Enfield-EAL-3-digit-serial-Mint", "Firearm");
            Set<ProductEntity> result = parser.parse(entity);
            Assert.assertEquals(1, result.size());
            ProductEntity product = result.iterator().next();
            Assert.assertEquals("", product.getJson());
        } finally {
            IOUtils.closeQuietly(pageStream);
        }
    }
}