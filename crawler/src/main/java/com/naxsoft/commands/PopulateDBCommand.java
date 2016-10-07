package com.naxsoft.commands;


import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsingService.WebPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;

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
//            "http://www.alflahertys.com/",
//            "http://www.bullseyelondon.com/",
            "http://www.cabelas.ca/",
//            "https://www.canadaammo.com/",
//            "http://www.canadiangunnutz.com/",
//            "https://www.corwin-arms.com/",
//            "http://www.crafm.com/",
//            "http://ctcsupplies.ca/",
//            "https://shop.dantesports.com/",
//            "https://ellwoodepps.com/",
//            "http://www.firearmsoutletcanada.com/",
//            "https://fishingworld.ca/",
//            "http://frontierfirearms.ca/",
//            "http://gotenda.com/",
//            "http://gun-shop.ca/",
//            "http://www.hical.ca/",
//            "http://internationalshootingsupplies.com/",
//            "https://www.irunguns.us/",
//            "http://www.leverarms.com/",
//            "http://www.magnumguns.ca/",
//            "http://www.marstar.ca/",
//            "http://store.prophetriver.com/",
//            "http://psmilitaria.50megs.com/",
//            "https://shopquestar.com/",
//            "http://www.sail.ca/",
//            "http://www.theammosource.com/",
//            "https://www.tradeexcanada.com/",
//            "http://westrifle.com/",
//            "http://www.wholesalesports.com/",
//            "https://www.wolverinesupplies.com/",
    };

    private final WebPageService webPageService;

    @Inject
    public PopulateDBCommand(WebPageService webPageService) {
        this.webPageService = webPageService;
    }


    @Override
    public void setUp() throws io.vertx.core.cli.CLIException {
    }

    @Override
    public void start() throws CLIException {
        Observable<String> roots = Observable.from(SOURCES);

        roots.observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .map(entry -> Observable.just(new WebPageEntity(null, "", "frontPage", false, entry, "")))
                .flatMap(webPageService::addWebPageEntry)
                .all(result -> result != 0L)
                .subscribe(result ->
                                LOGGER.info("Roots populated: {}", result),
                        err -> LOGGER.error("Failed to populate roots", err),
                        () -> LOGGER.info("Root population complete"));
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
