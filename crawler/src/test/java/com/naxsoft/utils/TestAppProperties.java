package com.naxsoft.utils;

import com.naxsoft.AbstractTest;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Copyright NAXSoft 2015
 */
public class TestAppProperties extends AbstractTest {
    @Test
    public void canReadProperties() {
        Assert.assertTrue(!AppProperties.getProperty("canadiangunnutzLogin").isEmpty());
    }
}
