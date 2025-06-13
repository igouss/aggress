package com.naxsoft.utils;

import java.util.function.Consumer;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Helper functions for Reactor
 */
class Rx {
    public static Consumer<Throwable> crashOnError() {
        final Throwable checkpoint = new Throwable();
        return throwable -> {
            StackTraceElement[] stackTrace = checkpoint.getStackTrace();
            StackTraceElement element = stackTrace[1]; // First element after `crashOnError()`
            String msg = String.format("onError() crash from subscribe() in %s.%s(%s:%s)",
                    element.getClassName(),
                    element.getMethodName(),
                    element.getFileName(),
                    element.getLineNumber());

            new Thread(() -> {
                throw new RuntimeException(msg, throwable);
            }).start();
        };
    }
}
