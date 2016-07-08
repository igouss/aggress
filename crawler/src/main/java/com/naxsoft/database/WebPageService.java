package com.naxsoft.database;

import com.github.davidmoten.rx.slf4j.Logging;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.TimeUnit;


/**
 *
 */
public class WebPageService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebPageService.class);

    protected Persistent database;

    /**
     * @param database Database driver
     */
    public WebPageService(Persistent database) {
        this.database = database;
    }

    /**
     * Persist webPage
     *
     * @param webPageEntity Webpage to persist
     * @return true if sucesfully persisted, false otherwise
     */
    public Observable<Long> save(WebPageEntity webPageEntity) {
        return database.save(webPageEntity);
    }


    /**
     * Update page parsed status in the database
     *
     * @param webPageEntity Page to update
     * @return The number of entities updated.
     */
    public Observable<? extends Number> markParsed(WebPageEntity webPageEntity) {
        LOGGER.debug("Marking {} {} as parsed", webPageEntity.getType(), webPageEntity.getUrl());
        return database.markWebPageAsParsed(webPageEntity);
    }


    /**
     * Get stream of unparsed pages.
     * Use scrolling
     *
     * @param type Webpage type
     * @return Stream of unparsed pages of specefied type
     * @see <a href="http://blog.danlew.net/2016/01/25/rxjavas-repeatwhen-and-retrywhen-explained/">RxJava's repeatWhen and retryWhen, explained</a>
     */
    public Observable<WebPageEntity> getUnparsedByType(String type) {
        return database.getUnparsedCount(type)
                .lift(Logging.<Long>logger("WebPageService::getUnparsedByType").onNextFormat(type + "=%s").log())
                .repeatWhen(observable -> {
                    LOGGER.info("Retrying getUnparsedByType {}", type);
                    return observable.delay(10, TimeUnit.SECONDS);
                }) // Poll for data periodically using repeatWhen + delay
                .takeWhile(val -> val != 0)
                .flatMap(count -> database.getUnparsedByType(type, count));
    }
}
