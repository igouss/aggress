package com.naxsoft;

import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.naxsoft.commands.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.out;
import static java.lang.System.setProperty;

public class Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

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

        return parser.parse(args);
    }

    private static void showHelp() {
        out.println("com.naxsoft.Crawler [-createESIndex] [-createESMappings] [-clean] [-populate] [-crawl] [-parse]");
    }

    public void start(String[] args) {
        ApplicationComponent applicationComponent = DaggerApplicationComponent.create();

        final ScheduledReporter elasticReporter = Slf4jReporter.forRegistry(applicationComponent.getMetricRegistry()).outputTo(LOGGER).build();

        try {
            final OptionSet options = parseCommandLineArguments(args);
            if (!options.hasOptions() || options.has("help")) {
                showHelp();
                return;
            }

            if (options.has("createESIndex")) {
                createESIndex(applicationComponent);
            }

            if (options.has("createESMappings")) {
                createESMappings(applicationComponent);
            }

            if (options.has("clean")) {
                cleanDb(applicationComponent);
            }

            if (options.has("populate")) {
                populateDb(applicationComponent);
            }

            if (options.has("crawl")) {
                crawl(applicationComponent);
            }

            if (options.has("parse")) {
                parse(applicationComponent);
            }
        } catch (Exception e) {
            LOGGER.error("Application failure", e);
        } finally {
            try {
                if (null != elasticReporter) {
                    elasticReporter.stop();
                }
                applicationComponent.getDatabase().close();
                applicationComponent.getElastic().close();
                applicationComponent.getHttpClient().close();
                ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Parse HTML pages stored in database in send them to elasticsearch for indexing
     *
     * @param applicationComponent
     */
    private void parse(ApplicationComponent applicationComponent) {
        Command parseCommand = new ParseCommand();
        parseCommand.setUp(applicationComponent);
        parseCommand.start();
        parseCommand.tearDown();
    }

    /**
     * Crawl websites for products and store product pages in database
     *
     * @param applicationComponent
     */
    private void crawl(ApplicationComponent applicationComponent) {
        Command crawlCommand = new CrawlCommand();
        crawlCommand.setUp(applicationComponent);
        crawlCommand.start();
        crawlCommand.tearDown();
    }

    /**
     * Populate database with initial dataset
     *
     * @param applicationComponent
     */
    private void populateDb(ApplicationComponent applicationComponent) {
        Command populateDBCommand = new PopulateDBCommand();
        populateDBCommand.setUp(applicationComponent);
        populateDBCommand.start();
        populateDBCommand.tearDown();
    }

    /**
     * Clean-up database from stale data
     *
     * @param applicationComponent
     */
    private void cleanDb(ApplicationComponent applicationComponent) {
        Command cleanDBCommand = new CleanDBCommand();
        cleanDBCommand.setUp(applicationComponent);
        cleanDBCommand.start();
        cleanDBCommand.tearDown();
    }

    /**
     * Prepare Elasticsearch mapping
     *
     * @param applicationComponent
     */
    private void createESMappings(ApplicationComponent applicationComponent) {
        Command command = new CreateESMappingCommand();
        command.setUp(applicationComponent);
        command.start();
        command.tearDown();
    }

    /**
     * Create elasticsearch index
     *
     * @param applicationComponent
     */
    private void createESIndex(ApplicationComponent applicationComponent) {
        Command command = new CreateESIndexCommand();
        command.setUp(applicationComponent);
        command.start();
        command.tearDown();
    }
}
