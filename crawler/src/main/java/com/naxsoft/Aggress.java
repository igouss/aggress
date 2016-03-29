package com.naxsoft;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.naxsoft.commands.*;
import com.naxsoft.database.Database;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

import static java.lang.System.out;
import static java.lang.System.setProperty;

public class Aggress {
    private static final Logger LOGGER = LoggerFactory.getLogger(Aggress.class);


    /**
     * Crawler application.
     * Walks websites, parses product pages and sends the result to elasticsearch.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Aggress aggress = new Aggress();
        aggress.start(args);
    }

    public void start(String[] args) {
//        Factory<Database> dbProvider = DatabaseModule_GetFactory.create(new DatabaseModule());
//        Factory<HttpClient> clientProvider = HttpClientModule_GetFactory.create(new HttpClientModule());
//        Aggress_MembersInjector.create(dbProvider, clientProvider).injectMembers(this);
        ApplicationComponent applicationComponent = DaggerApplicationComponent.create();


        final ScheduledReporter elasticReporter = Slf4jReporter.forRegistry(applicationComponent.getMetricRegistry()).outputTo(LOGGER).build();
        final OptionSet options = parseCommandLineArguments(args);


        if (!options.hasOptions() || options.has("help")) {
            showHelp();
            return;
        }
        try {
            applicationComponent.getElastic().connect("localhost", 9300);
        } catch (UnknownHostException e) {
            LOGGER.error("Failed to connect to elastic search", e);
            return;
        }

//            elasticReporter = ElasticsearchReporter.forRegistry(metrics)
//                    .hosts("localhost:9300")
//                    .build();
//            elasticReporter.start(1, TimeUnit.SECONDS);


        try {
            setProperty("jsse.enableSNIExtension", "false");
            setProperty("jdk.tls.trustNameService", "true");

            applicationComponent.getMetricRegistry().register(MetricRegistry.name(Database.class, "web_pages", "unparsed"), (Gauge<Long>) () -> applicationComponent.getDatabase().executeQuery(session -> {
                Query query = session.createQuery("select count(id) from WebPageEntity where parsed = false");
                return (Long) query.uniqueResult();
            }));

            if (options.has("createESIndex")) {
                Command command = new CreateESIndexCommand();
                command.setUp(applicationComponent);
                command.run();
                command.tearDown();
            }

            if (options.has("createESMappings")) {
                Command command = new CreateESMappingCommand();
                command.setUp(applicationComponent);
                command.run();
                command.tearDown();
            }

            if (options.has("clean")) {
                Command cleanDBCommand = new CleanDBCommand();
                cleanDBCommand.setUp(applicationComponent);
                cleanDBCommand.run();
                cleanDBCommand.tearDown();
            }

            if (options.has("populate")) {
                Command populateDBCommand = new PopulateDBCommand();
                populateDBCommand.setUp(applicationComponent);
                populateDBCommand.run();
                populateDBCommand.tearDown();
            }

            if (options.has("crawl")) {
                Command crawlCommand = new CrawlCommand();
                crawlCommand.setUp(applicationComponent);
                crawlCommand.run();
                crawlCommand.tearDown();
            }

            if (options.has("parse")) {
                Command parseCommand = new ParseCommand();
                parseCommand.setUp(applicationComponent);
                parseCommand.run();
                parseCommand.tearDown();
            }
        } catch (Exception e) {
            LOGGER.error("Application failure", e);
        } finally {
            if (null != elasticReporter) {
                elasticReporter.stop();
            }
            applicationComponent.getDatabase().close();
            applicationComponent.getElastic().close();
            applicationComponent.getHttpClient().close();

        }
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
        out.println("com.naxsoft.Aggress [-populate] [-clean] [-crawl] [-parse] [-createESIndex] [-createESMappings]");
    }

}
