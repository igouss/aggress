package com.naxsoft.modules;

import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for entity encoding services.
 * Replaces Dagger EncoderModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class EncoderModule {

    @Bean
    public WebPageEntityEncoder webPageEntityEncoder() {
        log.info("Creating WebPageEntityEncoder");
        return new WebPageEntityEncoder();
    }

    @Bean
    public ProductEntityEncoder productEntityEncoder() {
        log.info("Creating ProductEntityEncoder");
        return new ProductEntityEncoder();
    }
}
