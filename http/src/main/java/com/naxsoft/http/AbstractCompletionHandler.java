package com.naxsoft.http;

import okhttp3.Response;

import java.io.IOException;

/**
 * Base class to getParseRequestMessageHandler completed page download.
 * Logs on errors.
 * TODO: implement AsyncHandlerExtensions
 */
public abstract class AbstractCompletionHandler<R> {
    abstract public R onCompleted(Response response) throws IOException;
}
