package com.naxsoft.utils;

import com.naxsoft.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

public class TestAppProperties extends AbstractTest {
    @Test
    public void canReadProperties() {
        try {
            Assert.assertTrue(!AppProperties.getProperty("canadiangunnutzLogin").isEmpty());
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
}
