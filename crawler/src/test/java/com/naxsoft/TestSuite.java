package com.naxsoft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 *
 * Note: This class was updated to remove JUnit 4 dependencies.
 * Test execution is now handled by Gradle's JUnit Platform integration.
 * Use './gradlew test' to run tests.
 */
public class TestSuite {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class);

    public static void main(String[] args) {
        LOGGER.info("TestSuite is deprecated. Use './gradlew test' to run tests with JUnit 5 Platform.");
        System.out.println("Please use './gradlew test' to run tests with JUnit 5 Platform.");
    }
}