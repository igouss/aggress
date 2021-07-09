package com.naxsoft;

import org.junit.experimental.ParallelComputer;
import org.junit.experimental.max.MaxCore;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Set;

import static java.lang.System.out;

public class TestSuite {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class);

    public static void main(String[] args) {
        Path maxCorePath = Paths.get("", "maxCore").toAbsolutePath();
        File file = maxCorePath.toFile();
        MaxCore maxCore = MaxCore.storedLocally(file);
        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener(out));
        ParallelComputer computer = new ParallelComputer(true, true);

        Reflections reflections = new Reflections("com.naxsoft");
        Set<Class<? extends AbstractTest>> classes = reflections.getSubTypesOf(AbstractTest.class);

        LinkedList<Class<?>> included = new LinkedList<>();
        for (Class<? extends AbstractTest> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                included.add(clazz);
            }
        }
        try {
            Class<?>[] clazz = new Class<?>[]{};
            Request request = Request.classes(computer, included.toArray(clazz));
            maxCore.run(request, core);
        } catch (Exception e) {
            LOGGER.error("Failed run the test", e);
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
