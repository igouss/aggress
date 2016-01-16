package com.naxsoft;

import org.junit.experimental.ParallelComputer;
import org.junit.experimental.max.MaxCore;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class TestSuite {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);

    public static void main(String[] args) throws IOException {
        Path maxCorePath = Paths.get("", "maxCore").toAbsolutePath();
        File file = maxCorePath.toFile();
        MaxCore maxCore = MaxCore.storedLocally(file);
        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener(System.out));
        ParallelComputer computer = new ParallelComputer(true, true);

        Reflections reflections = new Reflections("com.naxsoft");
        Set<Class<? extends AbstractTest>> classes = reflections.getSubTypesOf(AbstractTest.class);

        for (Class<? extends AbstractTest> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    logger.info("Testing {}", clazz.getName());
                    Request request = Request.classes(computer, clazz);
                    Result result = maxCore.run(request, core);
                    System.out.println(result.wasSuccessful());
                } catch (Exception e) {
                    logger.error("Failed to instantiate test {}", clazz, e);
                }
            }
        }
    }
//    public static void main(String[] args) throws IOException {
//        JUnitCore core = new JUnitCore();
//        ParallelComputer computer = new ParallelComputer(true, true);
//
//        Reflections reflections = new Reflections("com.naxsoft");
//        Set<Class<? extends AbstractTest>> classes = reflections.getSubTypesOf(AbstractTest.class);
//
//        for (Class<? extends AbstractTest> clazz : classes) {
//            if (!Modifier.isAbstract(clazz.getModifiers())) {
//                try {
//                    logger.info("Instantiating {}", clazz.getName());
//                    Result result = core.run(computer,clazz);
//                    System.out.println(result.wasSuccessful());
//                } catch (Exception e) {
//                    logger.error("Failed to instantiate test {}", clazz, e);
//                }
//            }
//        }
//    }
}
