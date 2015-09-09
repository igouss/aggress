//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class WolverinesuppliesProductPageParser implements WebPageParser {
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        FetchClient client = new FetchClient();
        Set<WebPageEntity> result = new HashSet<>();
        Response response = client.get(webPage.getUrl());
        Logger logger = LoggerFactory.getLogger(this.getClass());
        if(response.statusCode() == 200) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(webPage.getUrl());
            webPageEntity.setParent(webPage);
            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            webPageEntity.setType("productPageRaw");
            webPageEntity.setContent(response.body());
            webPageEntity.setParent(webPage);
            result.add(webPageEntity);
        } else {
            logger.error("Failed to fetch from " + webPage.getUrl() + " errorCode=" + response.statusCode());
        }

        return result;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productPage");
    }
}
