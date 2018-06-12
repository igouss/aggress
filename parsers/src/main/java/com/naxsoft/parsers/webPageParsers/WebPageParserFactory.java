package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebPageParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPageParserFactory.class);

    Map<String, AbstractWebPageParser> parsers = new HashMap<>();

    /**
     * Create new WebPageParserFactory that using reflection to locate all WebPageParsers in the classpath
     *
     * @param client HTTP client for WebPageParsers to use
     */
    @Inject
    public WebPageParserFactory(HttpClient client) {
        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends AbstractWebPageParser>> classes = reflections.getSubTypesOf(AbstractWebPageParser.class);

        classes.stream().filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).forEach(clazz -> {
            try {
                Constructor<? extends AbstractWebPageParser> constructor = clazz.getDeclaredConstructor(HttpClient.class);
                constructor.setAccessible(true);
                AbstractWebPageParser webPageParser = constructor.newInstance(client);
                parsers.put(webPageParser.getSite() + "/" + webPageParser.getParserType(), webPageParser);
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate WebPage parser {}", clazz, e);
            }
        });
    }

    /**
     * Get a WebPageParser that is capable of parsing webPageEntity
     *
     * @param webPageEntity Page to parse
     */
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        String host = webPageEntity.getHost();
        return parsers.get(host + "/" + webPageEntity.getType()).parse(webPageEntity);
    }
}
