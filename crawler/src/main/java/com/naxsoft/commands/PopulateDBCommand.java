package com.naxsoft.commands;


import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.Semaphore;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Add initial dataset to the database.
 * The crawling is basically breath first search from that dataset
 */
public class PopulateDBCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(PopulateDBCommand.class);
    /**
     * Sites that crawler can walk and parse
     */
    private final static String[] SOURCES = {
            "http://www.alflahertys.com/",
            "http://www.bullseyelondon.com/",
            "http://www.cabelas.ca/",
            "https://www.canadaammo.com/",
            "http://www.canadiangunnutz.com/",
            "https://www.corwin-arms.com/",
            "http://www.crafm.com/",
            "http://ctcsupplies.ca/",
            "https://shop.dantesports.com/",
            "https://ellwoodepps.com/",
            "http://www.firearmsoutletcanada.com/",
            "https://fishingworld.ca/",
            "http://frontierfirearms.ca/",
            "http://gotenda.com/",
            "http://gun-shop.ca/",
            "http://www.hical.ca/",
            "http://internationalshootingsupplies.com/",
            "https://www.irunguns.us/",
            "http://www.leverarms.com/",
            "http://www.magnumguns.ca/",
            "http://www.marstar.ca/",
            "http://store.prophetriver.com/",
            "http://psmilitaria.50megs.com/",
            "https://shopquestar.com/",
            "http://www.sail.ca/",
            "http://www.theammosource.com/",
            "https://www.tradeexcanada.com/",
            "http://westrifle.com/",
            "http://www.wholesalesports.com/",
            "https://www.wolverinesupplies.com/",
    };
    private static WebPageService webPageService = null;

//    private static void populateSources() {
//        Observable.from(SOURCES).map(PopulateDBCommand::from)
//                .retry(3)
//                .doOnError(ex -> logger.error("Exception", ex))
//                .subscribe(PopulateDBCommand::save);
//    }

    /**
     * Create new SourceEntity object for with sourceUrl
     *
     * @param sourceUrl address of source page
     * @return
     */
    private static SourceEntity sourceEntityFromString(String sourceUrl) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setEnabled(true);
        sourceEntity.setUrl(sourceUrl);
        return sourceEntity;
    }

    /**
     * @param sourceEntity
     * @return
     */
    private static WebPageEntity webPageEntityFromSourceEntity(SourceEntity sourceEntity) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "frontPage", false, sourceEntity.getUrl(), "");
        LOGGER.info("Adding new root {}", webPageEntity.getUrl());
        return webPageEntity;
    }

    @Override
    public void setUp(ApplicationComponent applicationComponent) throws io.vertx.core.cli.CLIException {
        webPageService = applicationComponent.getWebPageService();
    }

    @Override
    public void start() throws CLIException {
        Semaphore semaphore = new Semaphore(0);
        Observable.from(SOURCES)
                .observeOn(Schedulers.io())
                .map(PopulateDBCommand::sourceEntityFromString)
                .map(PopulateDBCommand::webPageEntityFromSourceEntity)
                .flatMap(webPageService::save)
                .all(result -> result != 0L)
                .subscribe(result -> {
                            LOGGER.info("Roots populated: {}", result);
                        }, err -> {
                            LOGGER.error("Failed to populate roots", err);
                            semaphore.release();
                        },
                        () -> {
                            LOGGER.info("Root population complete");
                            semaphore.release();
                        });
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tearDown() throws CLIException {
        webPageService = null;
    }
}
