package com.naxsoft.database;

import com.naxsoft.entity.WebPageEntity;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.DirectedGraphBuilder;
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

    private DirectedGraphBuilder<WebPageEntity, DefaultEdge, DefaultDirectedGraph<WebPageEntity, DefaultEdge>> graphBuilder;

    /**
     * @param database Database driver
     */
    public WebPageService(Persistent database) {
        this.database = database;
        DefaultDirectedGraph<WebPageEntity, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graphBuilder = new DirectedGraphBuilder<>(graph);
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
