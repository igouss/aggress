package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class ProductParserFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductParserFacade.class);
    private final Set<ProductParser> parsers = new HashSet<>();

    public ProductParserFacade() {
        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser");
        Set<Class<? extends ProductParser>> classes = reflections.getSubTypesOf(ProductParser.class);

        for (Class<? extends ProductParser> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    LOGGER.info("Instantiating {}", clazz.getName());

                    Constructor<? extends ProductParser> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);

                    ProductParser e = constructor.newInstance();
                    parsers.add(e);
                } catch (Exception e) {
                    LOGGER.error("Failed to create a new product parser", e);
                }
            }
        }
    }

    /**
     * Get ProductParser capable of parsing webPageEntity
     *
     * @param webPageEntity page to parse
     * @return Parser capable of parsing the page
     */
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws ProductParseException {
        ProductParser parserToUse = null;
        for (ProductParser parser : parsers) {
            if (parser.canParse(webPageEntity)) {
                LOGGER.debug("Found a parser {} for action = {} url = {}", parser.getClass(), webPageEntity.getType(), webPageEntity.getUrl());
                parserToUse = parser;
                break;
            }
        }
        if (parserToUse == null) {
            LOGGER.warn("Failed to find a document parser for action = {} url = {}", webPageEntity.getType(), webPageEntity.getUrl());
            parserToUse = new NoopParser();
        }
        return parserToUse.parse(webPageEntity);
    }
}
