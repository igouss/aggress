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
    public Set<WebPageEntity> parse(String url) throws Exception {
        FetchClient client = new FetchClient();
        Set<WebPageEntity> result = new HashSet<>();
        Response response = client.get(url);
        Logger logger = LoggerFactory.getLogger(WolverinesuppliesProductPageParser.class);
        if(response.statusCode() == 200) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(url);
            webPageEntity.setSourceBySourceId(webPageEntity.getSourceBySourceId());
            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            webPageEntity.setType("productPageRaw");
            webPageEntity.setContent(response.body());
            result.add(webPageEntity);
        } else {
            logger.error("Failed to fetch from " + url + " errorCode=" + response.statusCode());
        }

        return result;
    }

    public boolean canParse(String url, String action) {
        return url.startsWith("https://www.wolverinesupplies.com/") && action.equals("productPage");
    }
}
