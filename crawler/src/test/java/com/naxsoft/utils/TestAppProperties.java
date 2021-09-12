package com.naxsoft.utils;

import com.naxsoft.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestAppProperties extends AbstractTest {
    @Test
    public void canReadProperties() {
        String canadiangunnutzLogin = assertDoesNotThrow(() -> AppProperties.getProperty("canadiangunnutzLogin"));
        assertFalse(canadiangunnutzLogin.isEmpty());
    }
}
