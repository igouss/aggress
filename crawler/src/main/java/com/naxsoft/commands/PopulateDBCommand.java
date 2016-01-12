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
 */
public class PopulateDBCommand implements Command {
    private final static Logger logger = LoggerFactory.getLogger(PopulateDBCommand.class);
    private final static String[] sources = {
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
            "http://www.hical.ca/",
            "https://www.irunguns.us/",
            "http://www.marstar.ca/",
            "http://www.sail.ca/",
            "http://www.theammosource.com/",
            "https://www.tradeexcanada.com/",
            "http://westrifle.com/",
            "http://www.wholesalesports.com/",
            "https://www.wolverinesupplies.com/",
            "http://www.leverarms.com/",
            "http://gun-shop.ca/",
            "https://shopquestar.com/",
    };
    private static WebPageService webPageService;
    private static SourceService sourceService;
//    private static void populateSources() {
//        Observable.from(sources).map(PopulateDBCommand::from)
//                .retry(3)
//                .doOnError(ex -> logger.error("Exception", ex))
//                .subscribe(PopulateDBCommand::save);
//    }

    private static void populateRoots() {
        Observable.from(sources).map(PopulateDBCommand::from).map(PopulateDBCommand::from)
                .subscribe(PopulateDBCommand::save, ex -> logger.error("PopulateRoots Exception", ex));
    }

    private static SourceEntity from(String sourceUrl) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setEnabled(true);
        sourceEntity.setUrl(sourceUrl);
        return sourceEntity;
    }

    private static boolean save(SourceEntity sourceEntity) {
        boolean rc = sourceService.save(sourceEntity);
        if (!rc) {
            logger.error("Failed to save sourceEntity");
        }
        return rc;
    }

    private static boolean save(WebPageEntity webPageEntities) {
        boolean rc = webPageService.save(webPageEntities);
        if (!rc) {
            logger.error("Failed to save webPageEntities");
        }
        return rc;
    }

    private static WebPageEntity from(SourceEntity sourceEntity) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(sourceEntity.getUrl());
        webPageEntity.setType("frontPage");
        logger.info("Adding new root {}", webPageEntity.getUrl());
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
