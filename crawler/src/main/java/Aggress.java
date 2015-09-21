//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.database.*;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParser;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class Aggress {
    static Logger logger;
    private static WebPageParserFactory webPageParserFactory;

    private static WebPageService webPageService;
    private static ProductService productService;
    private static SourceService sourceService;
    private static Elastic elastic;
    private static ProductParserFactory productParserFactory;

    public Aggress() {
    }

    public static void main(String[] args) {

        productParserFactory = new ProductParserFactory();

        Database db = null;

        logger = LoggerFactory.getLogger(Aggress.class);
        AsyncFetchClient asyncFetchClient = new AsyncFetchClient<>();
        try {
            webPageParserFactory = new WebPageParserFactory(asyncFetchClient);
            try {
                elastic = new Elastic();
                elastic.setup();
                logger.info("Elastic initialization complete");

                Client client = elastic.getClient();
                SearchResponse searchResponse = client.prepareSearch()
                        .setQuery(matchAllQuery())
                        .setFrom(0).setSize(2).setExplain(true)
                        .execute()
                        .actionGet();
                System.out.println(searchResponse);
            } catch (Exception e) {

                logger.error("Failed to initialize elastic", e);
                if (null != elastic) {
                    elastic.tearDown();
                }
            }

            try {
                db = new Database();
                db.setUp();
                logger.info("Database initialization complete");
            } catch (Exception e) {
                logger.error("Failed to initialize database", e);
                if (null != db) {
                    db.tearDown();
                }
            }

            webPageService = new WebPageService(db);
            productService = new ProductService(elastic, db);
            sourceService = new SourceService(db);

            String indexSuffix = "";//"-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            System.out.println(elastic.createIndex("product", "guns", indexSuffix));
            populateRoots();

//            webPageService.getUnparsedFrontPage().
//                    map(Aggress::parseObservableHelper).
//                    map(Aggress::parseObservable).
//                    map(Aggress::parseObservable).
//                    map(Aggress::productFromRawPage).
//                    subscribe(products -> products.subscribe(set -> productService.save(set)));



            process(webPageService.getUnparsedFrontPage());
            process(webPageService.getUnparsedProductList());
            process(webPageService.getUnparsedProductPage());
            logger.info("Fetch & parse complete");

            processProducts(webPageService.getUnparsedProductPageRaw());
            webPageService.deDup();
            indexProducts(productService.getProducts(), "product" + indexSuffix, "guns");
            productService.markAllAsIndexed();

            logger.info("Parsing complete");
        } catch (Exception e) {
            logger.error("Application failure", e);
        } finally {
            if (null != db) {
                db.tearDown();
            }
            if (null != elastic) {
                elastic.tearDown();
            }
            asyncFetchClient.close();
        }
    }

    private static Observable<WebPageEntity> parseObservable(Observable<WebPageEntity> webPageEntity) {
        return webPageEntity.flatMap(f -> parseObservableHelper(f)).filter(result -> result != null);
    }

    private static Observable<WebPageEntity> parseObservableHelper(WebPageEntity webPageEntity) {
        try {
            return webPageParserFactory.getParser(webPageEntity).parse(webPageEntity).flatMap(set -> Observable.from(set));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Observable<Set<ProductEntity>> productFromRawPage(Observable<WebPageEntity> productPageRaw) {
        return productPageRaw.map(f -> productFromRawPageHelper(f)).filter(result -> result != null);
    }

    private static Set<ProductEntity> productFromRawPageHelper(WebPageEntity webPageEntity) {
            try {
                return productParserFactory.getParser(webPageEntity).parse(webPageEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

    }

//    private static Set<ProductEntity> getWebPageEntitySetFunc1(WebPageEntity webPageEntity) {
//
//
//    }



    private static void indexProducts(Observable<ProductEntity> products, String index, String type) {
        elastic.index(products, index, type);
    }

    private static void populateRoots() {
        Observable<SourceEntity> sources = sourceService.getSources();
        sources.map(Aggress::from).toList().subscribe(Aggress::save);
        sourceService.markParsed(sources);
    }

    private static WebPageEntity from(SourceEntity sourceEntity) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(sourceEntity.getUrl());
        webPageEntity.setType("frontPage");
        logger.info("Adding new root + " + webPageEntity.getUrl());
        return webPageEntity;
    }

    private static void save(Collection<WebPageEntity> webPageEntities) {
        webPageService.save(webPageEntities);
    }

    private static void process(Observable<WebPageEntity> parents) {
        parents.doOnCompleted(() -> webPageService.deDup());
        parents.subscribe(webPageEntity -> {
            try {
                WebPageParser e = webPageParserFactory.getParser(webPageEntity);
                Observable<Set<WebPageEntity>> webPageEntitySet = e.parse(webPageEntity);
                webPageEntitySet.subscribe(webPageService::save, throwable -> {
                    logger.error("Failed to save web-page" + webPageEntity.getUrl(), e);
                });
                webPageService.markParsed(webPageEntity);
            } catch (Exception e1) {
                logger.error("Failed to process source " + webPageEntity.getUrl(), e1);
            }
        });

    }

    private static void saveParentToWebPage(WebPageEntity parent) {
    }

    private static void processProducts(Observable<WebPageEntity> webPage) {
        webPage.map(webPageEntity -> {
            ProductParser parser = productParserFactory.getParser(webPageEntity);
            Set<ProductEntity> result = new HashSet<>();
            if (parser.canParse(webPageEntity)) {
                try {
                    webPageService.markParsed(webPageEntity);
                    result.addAll(parser.parse(webPageEntity));
                    return result;
                } catch (Exception e) {
                    logger.error("Failed to parse product page " + webPageEntity.getUrl(), e);
                }
                return result;
            }
            return null;
        }).filter(data -> data != null).flatMap(Observable::from).toList().subscribe(productService::save);


    }
}
