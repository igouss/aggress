package com.naxsoft.modules;

import com.naxsoft.commands.*;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.Persistent;
import com.naxsoft.storage.elasticsearch.Elastic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for crawler command implementations.
 * Replaces Dagger CommandModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class CommandModule {

    @Bean
    public CleanDBCommand cleanDBCommand(Persistent persistent) {
        log.info("Creating CleanDBCommand");
        return new CleanDBCommand(persistent);
    }

    @Bean
    public CrawlCommand crawlCommand(WebPageService webPageService, WebPageParserFactory webPageParserFactory) {
        log.info("Creating CrawlCommand");
        return new CrawlCommand(webPageService, webPageParserFactory);
    }

    @Bean
    public CreateESIndexCommand createESIndexCommand(Elastic elastic) {
        log.info("Creating CreateESIndexCommand");
        return new CreateESIndexCommand(elastic);
    }

    @Bean
    public ParseCommand parseCommand(WebPageService webPageService, ProductParserFactory productParserFactory, Elastic elastic) {
        log.info("Creating ParseCommand");
        return new ParseCommand(webPageService, productParserFactory, elastic);
    }

    @Bean
    public PopulateDBCommand populateDBCommand(WebPageService webPageService) {
        log.info("Creating PopulateDBCommand");
        return new PopulateDBCommand(webPageService);
    }
}