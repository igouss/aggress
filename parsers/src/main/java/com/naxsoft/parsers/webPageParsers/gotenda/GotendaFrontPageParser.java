package com.naxsoft.parsers.webPageParsers.gotenda;

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
import java.util.stream.Collectors;

class GotendaFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GotendaFrontPageParser.class);

    public GotendaFrontPageParser(HttpClient client) {
        super(client);
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Set<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);
        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".HorizontalDisplay> li.NavigationElement > a");
            for (Element e : elements) {
                String url = e.attr("abs:href") + "&PageSize=60&Page=1";
                WebPageEntity webPageEntity = create(downloadResult.getSourcePage(), url, e.text());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    private Set<WebPageEntity> parseSubPages(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".InfoArea h3 a");

            List<WebPageEntity> webPageEntities = elements.stream()
                    .map(e -> create(downloadResult.getSourcePage(), e.attr("abs:href") + "&PageSize=60&Page=1", downloadResult.getSourcePage().getCategory()))
                    .collect(Collectors.toList());

            result.addAll(webPageEntities);
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.from(client.get(parent.getUrl(), new DocumentCompletionHandler(parent)))
                .map(this::parseFrontPage)
                .flatMap(Observable::from)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseSubPages)
                .flatMap(Observable::from)
                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "gotenda.com";
    }
}