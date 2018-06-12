package com.naxsoft.parsers.webPageParsers.grouseriver;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DocumentCompletionHandler;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GrouseriverFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrouseriverFrontPageParser.class);

    public GrouseriverFrontPageParser(HttpClient client) {
        super(client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".category-cell-name a");
            for (Element element : elements) {
                String category = "Home%2F" + element.attr("href").replaceAll("/", "%2F");
                String url = "http://www.grouseriver.com/api/items?include=facets&fieldset=search&language=en&country=CA&currency=CAD&pricelevel=5&c=3558148&n=2&category=" + category + "&limit=100&offset=0";
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", url, downloadResult.getSourcePage().getCategory());
                LOGGER.info("productList={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }

        }
        return result;
    }


    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.grouseriver.com/Firearms", "firearm"));
        webPageEntities.add(create(parent, "http://www.grouseriver.com/Optics", "optic"));

        return Observable.from(webPageEntities)
                .map(page -> client.get(page.getUrl(), new DocumentCompletionHandler(page)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "grouseriver.com";
    }
}