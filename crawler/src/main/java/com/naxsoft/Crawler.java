package com.naxsoft;

import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.naxsoft.commands.*;
import com.naxsoft.parsingService.ShellService;
import com.naxsoft.utils.HealthMonitor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static java.lang.System.out;
import static java.lang.System.setProperty;

public class Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);
    private static final Logger METRIC_LOGGER = LoggerFactory.getLogger("metrics");

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

    private static OptionSet parseCommandLineArguments(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("help");
        parser.accepts("populate");
        parser.accepts("clean");
        parser.accepts("crawl");
        parser.accepts("parse");
        parser.accepts("createESIndex");
        parser.accepts("createESMappings");
        parser.accepts("server");
        parser.accepts("remoteAccess");

        return parser.parse(args);
    }

    private static void showHelp() {
        out.println("com.naxsoft.Crawler [-createESIndex] [-clean] [-populate] [-crawl] [-parse] [-remoteAccess]");
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

        try {
            final OptionSet options = parseCommandLineArguments(args);
            if (!options.hasOptions() || options.has("help")) {
                showHelp();
                return;
            }

            CreateESIndexCommand createESIndexCommand = applicationComponent.getCreateESIndexCommand();
            CleanDBCommand cleanDbCommand = applicationComponent.getCleanDbCommand();
            PopulateDBCommand populateDBCommand = applicationComponent.getPopulateDBCommand();
            CrawlCommand crawlCommand = applicationComponent.getCrawlCommand();
            ParseCommand parseCommand = applicationComponent.getParseCommand();
            ShellService remoteAccess = applicationComponent.getRemoteAccess();

            if (options.has("createESIndex")) {
                createESIndexCommand.start();
            }

            if (options.has("clean")) {
                cleanDbCommand.start();

            }

            if (options.has("populate")) {
                populateDBCommand.start();
            }


            if (options.has("crawl")) {
                crawlCommand.start();
            }

            if (options.has("parse")) {
                parseCommand.start();
            }

            if (options.has("remoteAccess")) {
                remoteAccess.startHttpShellService();
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    LOGGER.info("Stopping crawler..");
                    metricReporter.stop();

                    if (options.has("createESIndex")) {
                        createESIndexCommand.tearDown();
                    }

                    if (options.has("clean")) {
                        cleanDbCommand.tearDown();

                    }

                    if (options.has("populate")) {
                        populateDBCommand.tearDown();
                    }

                    if (options.has("crawl")) {
                        crawlCommand.tearDown();
                    }

                    if (options.has("parse")) {
                        parseCommand.tearDown();
                    }
                    if (options.has("remoteAccess")) {
                        remoteAccess.stop();
                    }

                    applicationComponent.getWebPageParserFactory().close();
                    applicationComponent.getProductParserFactory().close();

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
