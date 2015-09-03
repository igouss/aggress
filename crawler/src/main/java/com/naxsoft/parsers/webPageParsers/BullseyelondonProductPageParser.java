package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Connection;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 */
public class BullseyelondonProductPageParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(String url) {
        FetchClient client = new FetchClient();
        Set<WebPageEntity> result = new HashSet<>();

        int retryCount = 5;
        while(true) {
            try {
                Connection.Response response = client.get(url);
                if (response.statusCode() == 200) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(url);
                    webPageEntity.setContent(response.body());
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(response.statusCode());
                    webPageEntity.setType("productPageRaw");
                    result.add(webPageEntity);
                    break;
                }
            } catch (IOException e) {
                if (0 == retryCount--) {
                    break;
                } else {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }

        return result;

    }

    @Override
    public boolean canParse(String url, String action) {
        return url.startsWith("http://www.bullseyelondon.com/") && action.equals("productPage");
    }
}
