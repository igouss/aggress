package com.naxsoft.parsers.webPageParsers.sail;

import com.naxsoft.AbstractTest;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Copyright NAXSoft 2015
 */
public class SailsProductListParserTest extends AbstractTest {
    @Test
    public void parse() {
    }
}