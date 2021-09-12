package com.naxsoft.parsingService;

import com.naxsoft.common.entity.WebPageEntity;
import com.naxsoft.storage.Persistent;

import java.util.List;

public class WebPageService {
    private final Persistent database;

    /**
     * @param database Database driver
     */
    public WebPageService(Persistent database) {
        this.database = database;
    }

    /**
     * Persist webPage
     *
     * @param webPageEntity WebPage to persist
     * @return true if successfully persisted, false otherwise
     */
    public Long addWebPageEntry(WebPageEntity webPageEntity) {
        return database.addWebPageEntry(webPageEntity);
    }

    /**
     * Update page parsed status in the database
     *
     * @param webPageEntity Page to update
     * @return The number of entities updated.
     */
    public Long markParsed(WebPageEntity webPageEntity) {
        return database.markWebPageAsParsed(webPageEntity);
    }


    /**
     * Get stream of un-parsed pages.
     * Use scrolling
     *
     * @param type WebPage type
     * @return Stream of unparsed pages of specefied type
     * @see <a href="http://blog.danlew.net/2016/01/25/rxjavas-repeatwhen-and-retrywhen-explained/">RxJava's repeatWhen and retryWhen, explained</a>
     */
    public List<WebPageEntity> getUnparsedByType(String type) {
        return database.getUnparsedByType(type);
    }
}
