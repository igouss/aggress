package com.naxsoft.parsers.webPageParsers.internationalshootingsupplies;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class InternationalshootingsuppliesFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternationalshootingsuppliesFrontPageParser.class);
    private static final Collection<Cookie> cookies = new ArrayList<>(1);

    static {
        cookies.add(new DefaultCookie("store_language", "english"));
    }

    private static WebPageEntity create(WebPageEntity parent, URL url, String category) {
        return new WebPageEntity(parent, "productList", url, category);
    }

    private Iterable<WebPageEntity> parseProductPage(WebPageEntity webPageEntity) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".general-pagination a");
            int max = 0;
            for (Element element : elements) {
                try {
                    int tmp = Integer.parseInt(element.text());
                    if (tmp > max) {
                        max = tmp;
                    }
                } catch (NumberFormatException ignore) {
                    // ignore
                }
            }
            if (max == 0) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", downloadResult.getSourcePage().getUrl(),
                        downloadResult.getSourcePage().getCategory());
                LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
                result.add(webPageEntity);
            } else {
                for (int i = 1; i <= max; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", downloadResult.getSourcePage().getUrl() + "page/" + i + "/",
                            downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), document.location());
                    result.add(webPageEntity);
                }
            }
        }
        return result;
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/ammunition/", "ammo"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/firearms/", "firearm"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/hunting-accessories/", "misc"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/optics/", "optic"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/reloading-components/", "reload"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/reloading-equipment/", "reload"));
        webPageEntities.add(create(parent, "http://internationalshootingsupplies.com/product-category/shooting-accessories/", "misc"));
        return Observable.from(webPageEntities)
                .observeOn(Schedulers.io())
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseProductPage)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "internationalshootingsupplies.com";
    }


}