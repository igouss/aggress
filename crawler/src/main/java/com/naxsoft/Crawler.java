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
        out.println("com.naxsoft.Crawler [-populate] [-clean] [-crawl] [-parse] [-createESIndex] [-createESMappings]");
    }

    public void start(String[] args) {
//        Factory<Database> dbProvider = DatabaseModule_GetFactory.create(new PersistentModule());
//        Factory<HttpClient> clientProvider = HttpClientModule_GetFactory.create(new HttpClientModule());
//        Aggress_MembersInjector.create(dbProvider, clientProvider).injectMembers(this);
        ApplicationComponent applicationComponent = DaggerApplicationComponent.create();


        final ScheduledReporter elasticReporter = Slf4jReporter.forRegistry(applicationComponent.getMetricRegistry()).outputTo(LOGGER).build();
        final OptionSet options = parseCommandLineArguments(args);


        if (!options.hasOptions() || options.has("help")) {
            showHelp();
            return;
        }

//            elasticReporter = ElasticsearchReporter.forRegistry(metrics)
//                    .hosts("localhost:9300")
//                    .build();
//            elasticReporter.start(1, TimeUnit.SECONDS);


        try {
            setProperty("jsse.enableSNIExtension", "false");
            setProperty("jdk.tls.trustNameService", "true");

            if (options.has("createESIndex")) {
                Command command = new CreateESIndexCommand();
                command.setUp(applicationComponent);
                command.start();
                command.tearDown();
            }

            if (options.has("createESMappings")) {
                Command command = new CreateESMappingCommand();
                command.setUp(applicationComponent);
                command.start();
                command.tearDown();
            }

            if (options.has("clean")) {
                Command cleanDBCommand = new CleanDBCommand();
                cleanDBCommand.setUp(applicationComponent);
                cleanDBCommand.start();
                cleanDBCommand.tearDown();
            }

            if (options.has("populate")) {
                Command populateDBCommand = new PopulateDBCommand();
                populateDBCommand.setUp(applicationComponent);
                populateDBCommand.start();
                populateDBCommand.tearDown();
            }

            if (options.has("crawl")) {
                Command crawlCommand = new CrawlCommand();
                crawlCommand.setUp(applicationComponent);
                crawlCommand.start();
                crawlCommand.tearDown();
            }

            if (options.has("parse")) {
                Command parseCommand = new ParseCommand();
                parseCommand.setUp(applicationComponent);
                parseCommand.start();
                parseCommand.tearDown();
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
}
