package com.naxsoft.utils;

import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action1;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Helper gunctions for RxJava
 */
class Rx {
    public static Action1<Throwable> crashOnError() {
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
                throw new OnErrorNotImplementedException(msg, throwable);
            }).start();
        };
    }
}
