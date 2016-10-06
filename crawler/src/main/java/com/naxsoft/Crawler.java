package com.naxsoft;

import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.naxsoft.scheduler.Scheduler;
import com.naxsoft.utils.HealthCheck;
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
        HealthCheck.start();
        DaggerApplicationComponent.Builder applicationBuilder = DaggerApplicationComponent.builder();
        ApplicationComponent applicationComponent = applicationBuilder.build();

        final ScheduledReporter elasticReporter = Slf4jReporter.forRegistry(applicationComponent.getMetricRegistry()).outputTo(LOGGER).build();
        Scheduler scheduler = null;

        try {
            final OptionSet options = parseCommandLineArguments(args);
            if (!options.hasOptions() || options.has("help")) {
                showHelp();
                return;
            }

            if (options.has("createESIndex")) {
                applicationComponent.getCreateESIndexCommand().start();
            }

//            scheduler = applicationComponent.getScheduler();
//            scheduler.add(() -> {

            if (options.has("clean")) {
                applicationComponent.getCleanDbCommand().start();

            }

            if (options.has("populate")) {
                applicationComponent.getPopulateDBCommand().start();
            }

            if (options.has("crawl")) {
                applicationComponent.getCrawlCommand().start();
            }

            if (options.has("parse")) {
                applicationComponent.getParseCommand().start();
            }
//            }, 0, 1, TimeUnit.MINUTES);
            System.in.read();
        } catch (Exception e) {
            LOGGER.error("Application failure", e);
        } finally {
            try {
                if (null != elasticReporter) {
                    elasticReporter.stop();
                }
                if (scheduler != null) {
                    scheduler.stop();
                }
                applicationComponent.getDatabase().close();
                applicationComponent.getHttpClient().close();
                applicationComponent.getWebPageParserFactory().close();
                applicationComponent.getProductParserFactory().close();
                applicationComponent.getElastic().close();
                ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
                applicationComponent.getElastic().close();
                applicationComponent.getVertx().close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
