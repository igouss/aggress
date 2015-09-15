package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Connection;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CabellasProductPageParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Connection.Response response = client.get(webPage.getUrl());
        if (response.statusCode() == 200) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(webPage.getUrl());
            webPageEntity.setContent(response.body());
            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            webPageEntity.setParsed(false);
            webPageEntity.setStatusCode(Integer.valueOf(response.statusCode()));
            webPageEntity.setType("productPageRaw");
            webPageEntity.setParent(webPage);
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("productPage");
    }
}
