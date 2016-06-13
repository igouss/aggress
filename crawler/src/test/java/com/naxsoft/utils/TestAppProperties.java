package com.naxsoft.utils;

import com.naxsoft.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Copyright NAXSoft 2015
 */
public class TestAppProperties extends AbstractTest {
    @Test
    public void canReadProperties() {
        try {
            Assert.assertTrue(!AppProperties.getProperty("canadiangunnutzLogin").getValue().isEmpty());
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
}
