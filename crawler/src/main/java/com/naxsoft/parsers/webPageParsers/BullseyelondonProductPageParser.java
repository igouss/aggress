//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Connection.Response;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class BullseyelondonProductPageParser implements WebPageParser {
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Response response = client.get(webPage.getUrl());
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

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("productPage");
    }
}
