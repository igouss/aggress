package com.naxsoft.parsingService;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.storage.Persistent;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class WebPageService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebPageService.class);

    /**
     *
     */
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
    public Flowable<Long> addWebPageEntry(WebPageEntity webPageEntity) {
        return database.addWebPageEntry(webPageEntity);
    }

    /**
     * Update page parsed status in the database
     *
     * @param webPageEntity Page to update
     * @return The number of entities updated.
     */
    public Flowable<Long> markParsed(WebPageEntity webPageEntity) {
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
    public Flowable<WebPageEntity> getUnparsedByType(String type) {
        return database.getUnparsedCount(type)
                .doOnNext(val -> LOGGER.info("Found {} of type {}", val, type))
                .filter(count -> count != 0)
                .flatMap(count -> database.getUnparsedByType(type, count))
                .doOnNext(val -> LOGGER.info("Found unparsed {} {} {}", val.getType(), val.getUrl(), val.getCategory()));
    }
}
