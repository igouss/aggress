package com.naxsoft.crawler;

import com.naxsoft.parsers.webPageParsers.DownloadResult;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class to getParseRequestMessageHandler completed page download.
 * Logs on errors.
 * TODO: implement AsyncHandlerExtensions
 */
public abstract class AbstractCompletionHandler<R> {
    abstract public R onCompleted(Response response) throws Exception;
}
