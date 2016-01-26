package com.naxsoft.commands;


import com.naxsoft.ExecutionContext;
import com.naxsoft.database.SourceService;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 *
 * Add initial dataset to the database.
 * The crawling is basically breath first search from that dataset
 *
 */
public class PopulateDBCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(PopulateDBCommand.class);
    private static WebPageService webPageService = null;
    private static SourceService sourceService = null;

    /**
     * Sites that crawler can walk and parse
     */
    private final static String[] SOURCES = {
//            "http://www.alflahertys.com/",
//            "http://www.bullseyelondon.com/",
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
//            "http://www.hical.ca/",
//            "https://www.irunguns.us/",
//            "http://www.marstar.ca/",
//            "http://www.sail.ca/",
//            "http://www.theammosource.com/",
            "https://www.tradeexcanada.com/",
//            "http://westrifle.com/",
//            "http://www.wholesalesports.com/",
//            "https://www.wolverinesupplies.com/",
//            "http://www.leverarms.com/",
//            "http://gun-shop.ca/",
//            "https://shopquestar.com/",
    };

//    private static void populateSources() {
//        Observable.from(SOURCES).map(PopulateDBCommand::from)
//                .retry(3)
//                .doOnError(ex -> logger.error("Exception", ex))
//                .subscribe(PopulateDBCommand::save);
//    }

    private static void populateRoots() {
        Observable.from(SOURCES).map(PopulateDBCommand::from).map(PopulateDBCommand::from)
                .subscribe(PopulateDBCommand::save, ex -> LOGGER.error("PopulateRoots Exception", ex));
    }

    private static SourceEntity from(String sourceUrl) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setEnabled(true);
        sourceEntity.setUrl(sourceUrl);
        return sourceEntity;
    }

    /**
     *
     * @param sourceEntity
     * @return
     */
    private static boolean save(SourceEntity sourceEntity) {
        boolean rc = sourceService.save(sourceEntity);
        if (!rc) {
            LOGGER.error("Failed to save sourceEntity");
        }
        return rc;
    }

    /**
     *
     * @param webPageEntities
     * @return
     */
    private static boolean save(WebPageEntity webPageEntities) {
        boolean rc = webPageService.save(webPageEntities);
        if (!rc) {
            LOGGER.error("Failed to save webPageEntities");
        }
        return rc;
    }

    /**
     *
     * @param sourceEntity
     * @return
     */
    private static WebPageEntity from(SourceEntity sourceEntity) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(sourceEntity.getUrl());
        webPageEntity.setType("frontPage");
        LOGGER.info("Adding new root {}", webPageEntity.getUrl());
        return webPageEntity;
    }

    @Override
    public void setUp(ExecutionContext context) throws io.vertx.core.cli.CLIException {
        webPageService = context.getWebPageService();
        sourceService = context.getSourceService();
    }

    @Override
    public void run() throws CLIException {
//        populateSources();
        populateRoots();
    }

    @Override
    public void tearDown() throws CLIException {
        webPageService = null;
        sourceService = null;
    }

}
