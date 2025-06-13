package com.naxsoft;

import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.naxsoft.commands.*;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.modules.*;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.scheduler.Scheduler;
import com.naxsoft.storage.Persistent;
import com.naxsoft.storage.elasticsearch.Elastic;
import com.naxsoft.utils.HealthMonitor;
import io.vertx.core.Vertx;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

import static java.lang.System.out;
import static java.lang.System.setProperty;

/**
 * Spring Boot application entry point for the Aggress Web Crawler.
 * Replaces the Dagger-based ApplicationComponent with Spring native dependency injection.
 * <p>
 * This provides:
 * - Spring Boot autoconfiguration and dependency injection
 * - Command-line interface for crawler operations
 * - Health checks via Actuator
 * - Configuration management via @ConfigurationProperties
 * - Metrics reporting and monitoring
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@Import({
        RedisModule.class,
        HttpClientModule.class,
        ElasticModule.class,
        MetricsRegistryModule.class,
        WebPageServiceModule.class,
        ProductServiceModule.class,
        WebPageParserFactoryModule.class,
        ProductParserFactoryModule.class,
        EncoderModule.class,
        EventBusModule.class,
        CommandModule.class,
        SchedulerModule.class,
        VertxModule.class
})
@Slf4j
public class CrawlerApplication implements CommandLineRunner {

    // Spring Boot will automatically inject these dependencies
    @Autowired
    private Persistent database;
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private Elastic elastic;
    @Autowired
    private WebPageParserFactory webPageParserFactory;
    @Autowired
    private ProductParserFactory productParserFactory;
    @Autowired
    private MetricRegistry metricRegistry;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private CleanDBCommand cleanDBCommand;
    @Autowired
    private CrawlCommand crawlCommand;
    @Autowired
    private CreateESIndexCommand createESIndexCommand;
    @Autowired
    private ParseCommand parseCommand;
    @Autowired
    private PopulateDBCommand populateDBCommand;
    @Autowired
    private Vertx vertx;

    public static void main(String[] args) {
        setProperty("jsse.enableSNIExtension", "false");
        setProperty("jdk.tls.trustNameService", "true");

        log.info("Starting Aggress Crawler with Spring Boot 3.5...");
        SpringApplication.run(CrawlerApplication.class, args);
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
        return parser.parse(args);
    }

    private static void showHelp() {
        out.println("com.naxsoft.CrawlerApplication [-createESIndex] [-clean] [-populate] [-crawl] [-parse]");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Aggress Crawler started with Spring Boot dependency injection");

        HealthMonitor healthMonitor = new HealthMonitor();
        healthMonitor.start();

        final ScheduledReporter metricReporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("metrics"))
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

            if (options.has("createESIndex")) {
                createESIndexCommand.start();
            }

            if (options.has("clean")) {
                cleanDBCommand.start();
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

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Stopping Spring Boot crawler...");
                    metricReporter.stop();

                    if (options.has("createESIndex")) {
                        createESIndexCommand.tearDown();
                    }
                    if (options.has("clean")) {
                        cleanDBCommand.tearDown();
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

                    webPageParserFactory.close();
                    productParserFactory.close();
                    database.close();
                    httpClient.close();
                    elastic.close();
                    vertx.close();
                    healthMonitor.stop();

                    log.info("Spring Boot crawler stopped successfully");
                    ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
                } catch (Exception e) {
                    log.error("Error during shutdown", e);
                }
            }, "Shutdown Hook"));

            System.in.read();
        } catch (Exception e) {
            log.error("Crawler application failure", e);
        }
    }
}