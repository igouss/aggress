//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WolverinesuppliesProductPageParser implements WebPageParser {
    public WolverinesuppliesProductPageParser() {
    }

    public Set<WebPageEntity> parse(String url) throws Exception {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Response response = client.get(url);
        Logger logger = LoggerFactory.getLogger(WolverinesuppliesProductPageParser.class);
        if(response.statusCode() == 200) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(url);
            webPageEntity.setSourceBySourceId(webPageEntity.getSourceBySourceId());
            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            webPageEntity.setType("productPageRaw");
            String body = response.body();
            webPageEntity.setContent(body);
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
