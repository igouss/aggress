package com.naxsoft.modules;

import com.naxsoft.parsingService.ProductService;
import com.naxsoft.storage.Persistent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot configuration for product service.
 * Replaces Dagger ProductServiceModule with Spring native dependency injection.
 */
@Configuration
@Import(RedisModule.class)
@Slf4j
public class ProductServiceModule {

    @Bean
    public ProductService productService(Persistent persistent) {
        log.info("Creating ProductService");
        return new ProductService(persistent);
    }
}
