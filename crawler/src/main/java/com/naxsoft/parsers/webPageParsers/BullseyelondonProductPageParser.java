//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.jsoup.Connection.Response;

public class BullseyelondonProductPageParser implements WebPageParser {
    public BullseyelondonProductPageParser() {
    }

    public Set<WebPageEntity> parse(String url) {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        int retryCount = 5;

        while(true) {
            try {
                Response e = client.get(url);
                if(e.statusCode() == 200) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(url);
                    webPageEntity.setContent(e.body());
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(Integer.valueOf(e.statusCode()));
                    webPageEntity.setType("productPageRaw");
                    result.add(webPageEntity);
                    break;
                }
            } catch (IOException var8) {
                if(0 == retryCount--) {
                    break;
                }

                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
                } catch (InterruptedException var7) {
                    ;
                }
            }
        }

        return result;
    }

    public boolean canParse(String url, String action) {
        return url.startsWith("http://www.bullseyelondon.com/") && action.equals("productPage");
    }
}
