//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

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
    public Observable<Boolean> save(WebPageEntity webPageEntity) {
        return database.save(webPageEntity);
    }

    /**
     * Keep producing unparsedCount till it is equals to 0
     *
     * @param type Column type to check against
     * @return row count in type
     */
    private Observable<Long> getUnparsedCount(String type) {
        return database.getUnparsedCount(type);
//        return Observable.create(subscriber -> {
//            while (!subscriber.isUnsubscribed()) {
//
//                unparsedCount.subscribe(value -> {
//                    if (0L == value) {
//                        subscriber.onCompleted();
//                    } else {
//                        subscriber.onNext(value);
//                    }
//                });
//            }
//        });
    }

    /**
     * Update page parsed status in the database
     *
     * @param webPageEntity Page to update
     * @return The number of entities updated.
     */
    public Observable<Integer> markParsed(WebPageEntity webPageEntity) {
//        Observable<Integer> rc;
//        if (0 == webPageEntity.getId()) {
////            LOGGER.error("Trying to save a webpage with id=0 {}", webPageEntity);
//            throw new RuntimeException("Trying to save a webpage with id=0" + webPageEntity);
//        } else {
//            rc = database.markWebPageAsParsed(webPageEntity);
//        }
        return database.markWebPageAsParsed(webPageEntity);
    }


    /**
     * Get stream of unparsed pages.
     * Use scrolling
     *
     * @param type Webpage type
     * @return Stream of unparsed pages of specefied type
     */
    public Observable<WebPageEntity> getUnparsedByType(String type) {
        return getUnparsedCount(type).flatMap(count -> database.getUnparsedByType(type));
    }
}
