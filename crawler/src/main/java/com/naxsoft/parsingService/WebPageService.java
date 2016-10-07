package com.naxsoft.parsingService;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.storage.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class WebPageService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebPageService.class);

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
    public Observable<Long> addWebPageEntry(Observable<WebPageEntity> webPageEntity) {
        return database.addWebPageEntry(webPageEntity);
    }

    /**
     * Update page parsed status in the database
     *
     * @param webPageEntity Page to update
     * @return The number of entities updated.
     */
    public Observable<Long> markParsed(WebPageEntity webPageEntity) {
        LOGGER.info("Marking {} {} as parsed", webPageEntity.getType(), webPageEntity.getUrl());
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
    public Observable<WebPageEntity> getUnparsedByType(String type, long delay, TimeUnit timeUnit) {
        return database.getUnparsedCount(type)
                .repeatWhen(observable -> {
                    LOGGER.info("Retrying getUnparsedByType {} {} {}", type, delay, timeUnit);
                    return observable.delay(delay, timeUnit);
                }) // Poll for data periodically using repeatWhen + delay
//                .takeWhile(val -> val != 0)
                .doOnNext(val -> LOGGER.info("Found {} of type {}", val, type))
                .filter(count -> count != 0)
                .flatMap(count -> database.getUnparsedByType(type, count));
    }
}
