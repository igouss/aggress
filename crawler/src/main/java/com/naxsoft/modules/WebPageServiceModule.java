package com.naxsoft.modules;

import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.Persistent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot configuration for web page service.
 * Replaces Dagger WebPageServiceModule with Spring native dependency injection.
 */
@Configuration
@Import(RedisModule.class)
@Slf4j
public class WebPageServiceModule {

    @Bean
    public WebPageService webPageService(Persistent persistent) {
        log.info("Creating WebPageService");
        return new WebPageService(persistent);
    }
}
