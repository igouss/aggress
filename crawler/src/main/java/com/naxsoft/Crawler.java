package com.naxsoft;

import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.naxsoft.commands.*;
import com.naxsoft.scheduler.Scheduler;
import com.naxsoft.utils.HealthMonitor;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static java.lang.System.out;
import static java.lang.System.setProperty;

public class Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);
    private static final Logger METRIC_LOGGER = LoggerFactory.getLogger("metrics");
    private CreateESIndexCommand createESIndexCommand = null;
    private CleanDBCommand cleanDbCommand = null;
    private PopulateDBCommand populateDBCommand = null;
    private CrawlCommand crawlCommand = null;
    private ParseCommand parseCommand = null;

    /**
     * Crawler application.
     * Walks websites, parses product pages and sends the result to elasticsearch.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        setProperty("jsse.enableSNIExtension", "false");
        setProperty("jdk.tls.trustNameService", "true");

        Crawler aggress = new Crawler();
        aggress.start(args);
        System.exit(0);
    }

    private static void showHelp() {
        out.println("com.naxsoft.Crawler [-createESIndex] [-clean] [-populate] [-crawl] [-parse]");
    }

    private void start(String[] args) {
        HealthMonitor healthMonitor = new HealthMonitor();
        healthMonitor.start();
        DaggerApplicationComponent.Builder applicationBuilder = DaggerApplicationComponent.builder();
        ApplicationComponent applicationComponent = applicationBuilder.build();

        final ScheduledReporter metricReporter = Slf4jReporter.forRegistry(applicationComponent.getMetricRegistry())
                .outputTo(METRIC_LOGGER)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        metricReporter.start(30, TimeUnit.SECONDS);

        Scheduler scheduler = null;


        try {
            final OptionSet options = CommandLineParserKt.parse(args);
            if (!options.hasOptions() || options.has("help")) {
                showHelp();
                return;
            }

            if (options.has("createESIndex")) {
                createESIndexCommand = applicationComponent.getCreateESIndexCommand();
                createESIndexCommand.start();
            }

//            scheduler = applicationComponent.getScheduler();
//            scheduler.add(() -> {

            if (options.has("clean")) {
                cleanDbCommand = applicationComponent.getCleanDbCommand();
                cleanDbCommand.start();

            }

            if (options.has("populate")) {
                populateDBCommand = applicationComponent.getPopulateDBCommand();
                populateDBCommand.start();
            }

            if (options.has("crawl")) {
                crawlCommand = applicationComponent.getCrawlCommand();
                crawlCommand.start();
            }

            if (options.has("parse")) {
                parseCommand = applicationComponent.getParseCommand();
                parseCommand.start();
            }
//            }, 0, 1, TimeUnit.MINUTES);


            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    LOGGER.info("Stopping crawler..");
                    metricReporter.stop();
                    if (scheduler != null) {
                        scheduler.stop();
                    }

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

                    applicationComponent.getDatabase().close();
                    applicationComponent.getHttpClient().close();
                    applicationComponent.getElastic().close();
                    applicationComponent.getVertx().close();
                    healthMonitor.stop();
                    LOGGER.info("Crawler stopped...");

                    ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Shutdown Hook"));

            System.in.read();
        } catch (Exception e) {
            LOGGER.error("Application failure", e);
        }
    }
}
