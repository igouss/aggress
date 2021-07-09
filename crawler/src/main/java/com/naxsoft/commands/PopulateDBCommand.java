package com.naxsoft.commands;


import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.utils.SitesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Add initial dataset to the database.
 * The crawling is basically breath first search from that dataset
 */
@Slf4j
@RequiredArgsConstructor
public class PopulateDBCommand implements Command {
    private final WebPageService webPageService;

    @Override
    public void setUp() {
    }

    @Override
    public void start() throws CLIException {
        Observable<String> roots = Observable.from(SitesUtil.SOURCES);

        roots.observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .map(entry -> new WebPageEntity(null, "", "frontPage", entry, ""))
                .map(webPageService::addWebPageEntry)
                .all(result -> result != 0L)
                .subscribe(
                        result -> log.trace("Roots populated: {}", result)
                        , err -> log.error("Failed to populate roots", err)
                        , () -> log.info("Root population complete")
                );
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
