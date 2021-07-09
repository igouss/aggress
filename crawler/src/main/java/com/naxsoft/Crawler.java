package com.naxsoft;

import com.naxsoft.commands.*;
import joptsimple.OptionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.lang.System.out;
import static java.lang.System.setProperty;

@Slf4j
@RequiredArgsConstructor
public class Crawler {
    private final CreateESIndexCommand createESIndexCommand;
    private final CleanDBCommand cleanDbCommand;
    private final PopulateDBCommand populateDBCommand;
    private final CrawlCommand crawlCommand;
    private final ParseCommand parseCommand;

    /**
     * Crawler application.
     * Walks websites, parses product pages and sends the result to elasticsearch.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        setProperty("jsse.enableSNIExtension", "false");
        setProperty("jdk.tls.trustNameService", "true");

//        Crawler aggress = new Crawler();
//        aggress.start(args);
        System.exit(0);
    }

    private static void showHelp() {
        out.println("com.naxsoft.Crawler [-createESIndex] [-clean] [-populate] [-crawl] [-parse]");
    }

    private void start(String[] args) {
            final OptionSet options = CommandLineParser.parse(args);
            if (!options.hasOptions() || options.has("help")) {
                showHelp();
                return;
            }

//            if (options.has("createESIndex")) {
//                createESIndexCommand = applicationComponent.getCreateESIndexCommand();
//                createESIndexCommand.start();
//            }
//
////            scheduler = applicationComponent.getScheduler();
////            scheduler.add(() -> {
//
//            if (options.has("clean")) {
//                cleanDbCommand = applicationComponent.getCleanDbCommand();
//                cleanDbCommand.start();
//
//            }
//
//            if (options.has("populate")) {
//                populateDBCommand = applicationComponent.getPopulateDBCommand();
//                populateDBCommand.start();
//            }
//
//            if (options.has("crawl")) {
//                crawlCommand = applicationComponent.getCrawlCommand();
//                crawlCommand.start();
//            }
//
//            if (options.has("parse")) {
//                parseCommand = applicationComponent.getParseCommand();
//                parseCommand.start();
//            }
//            }, 0, 1, TimeUnit.MINUTES);


            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Stopping crawler..");

                    if (createESIndexCommand != null) {
                        createESIndexCommand.tearDown();
                    }

//            scheduler = applicationComponent.getScheduler();
//            scheduler.add(() -> {

                    if (cleanDbCommand != null) {
                        cleanDbCommand.tearDown();

                    }

                    if (populateDBCommand != null) {
                        populateDBCommand.tearDown();
                    }

                    if (crawlCommand != null) {
                        crawlCommand.tearDown();
                    }

                    if (parseCommand != null) {
                        parseCommand.tearDown();
                    }

//                    applicationComponent.getDatabase().close();
//                    applicationComponent.getHttpClient().close();
//                    applicationComponent.getElastic().close();
                    log.info("Crawler stopped...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Shutdown Hook"));
    }
}
