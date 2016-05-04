package com.naxsoft.commands;


import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

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
            "http://www.bullseyelondon.com/",
//            "http://www.cabelas.ca/",
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
    private static WebPageService webPageService = null;

//    private static void populateSources() {
//        Observable.from(SOURCES).map(PopulateDBCommand::from)
//                .retry(3)
//                .doOnError(ex -> logger.error("Exception", ex))
//                .subscribe(PopulateDBCommand::save);
//    }

    private static SourceEntity from(String sourceUrl) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setEnabled(true);
        sourceEntity.setUrl(sourceUrl);
        return sourceEntity;
    }

    /**
     * @param webPageEntities
     * @return
     */
    private static Observable<Boolean> save(WebPageEntity webPageEntities) {
        Observable<Boolean> rc = webPageService.save(webPageEntities);
        return rc;
    }

    /**
     * @param sourceEntity
     * @return
     */
    private static WebPageEntity from(SourceEntity sourceEntity) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "frontPage", false, sourceEntity.getUrl(), "");
        LOGGER.info("Adding new root {}", webPageEntity.getUrl());
        return webPageEntity;
    }

    @Override
    public void setUp(ApplicationComponent applicationComponent) throws io.vertx.core.cli.CLIException {
        webPageService = applicationComponent.getWebPageService();
    }

    @Override
    public void run() throws CLIException {
        Observable.from(SOURCES).map(PopulateDBCommand::from).map(PopulateDBCommand::from)
                .flatMap(PopulateDBCommand::save)
                .all(result -> result == Boolean.TRUE)
                .subscribe(result -> {
                    LOGGER.info("Roots populated: {}", result);
                }, err -> {
                    LOGGER.error("Failed to populate roots", err);
                });
    }

    @Override
    public void tearDown() throws CLIException {
        webPageService = null;
    }
}
