package com.naxsoft.aggress.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class AppPropertiesTest {
    @Test
    public void canReadProperties() {
        String canadiangunnutzLogin = Assertions.assertDoesNotThrow(() -> AppProperties.getProperty("canadiangunnutzLogin"));
        assertFalse(canadiangunnutzLogin.isEmpty());
    }
}
