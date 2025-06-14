package com.naxsoft.utils;

import com.naxsoft.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class TestAppProperties extends AbstractTest {
    @Test
    public void canReadProperties() {
        try {
            assertTrue(!AppProperties.getProperty("canadiangunnutzLogin").isEmpty());
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }
    }
}
