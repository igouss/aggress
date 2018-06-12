package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Set;

public class ProductParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductParserFactory.class);

    private final ArrayList<String> parserVertex;


    @Inject
    public ProductParserFactory() {
        parserVertex = new ArrayList<>();
        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser");
        Set<Class<? extends AbstractRawPageParser>> classes = reflections.getSubTypesOf(AbstractRawPageParser.class);
    }

    /**
     * Get ProductParser capable of parsing webPageEntity
     *
     * @param webPageEntity page to parse
     * @return Parser capable of parsing the page
     */
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
        return null;
    }
}
