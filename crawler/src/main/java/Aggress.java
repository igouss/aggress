//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.codahale.metrics.MetricRegistry;
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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class Aggress {
    public static final MetricRegistry metrics = new MetricRegistry();
    public static final Thread CURRENT_THREAD = Thread.currentThread();
    static Logger logger;

    public Aggress() {
    }

    public static void main(String[] args) {


        Database db = null;
        Elastic elastic = null;
        logger = LoggerFactory.getLogger(Aggress.class);

        try {
            WebPageParserFactory webPageParserFactory = new WebPageParserFactory();
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



            WebPageService webPageService = new WebPageService(db);
            ProductService productService = new ProductService(elastic, db);
            SourceService sourceService = new SourceService(db);

//
//
//            webPageService.getUnparsedFrontPageAsync()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(Schedulers.io())
//                    .concatWith(webPageService.getUnparsedProductListAsync())
//                    .concatWith(webPageService.getUnparsedProductPageAsync())
//
//                    .subscribe(wpe -> {
//                        processAsync(wpe, webPageParserFactory, webPageService);
//                    });
//            webPageService.getUnparsedProductPageRawAsync().subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
//                    .subscribe(webPageEntity -> {
//                        processAsync(webPageEntity, webPageService, productService);
//                    });


//            webPageService.getAsync2("from WebPageEntity").
//                    subscribeOn(Schedulers.io()).
//                    observeOn(Schedulers.computation()).
//                    doOnEach(wpe -> logger.debug("processing" + wpe.getValue())).
//                    doOnCompleted(() -> logger.info("Processing complete")).
//                    doOnError(e -> logger.error("Failed to process web-page", e)).
//                    subscribe();

            String indexSuffix = "-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//            System.out.println(elastic.createIndex("product", "guns", indexSuffix));
//            populateRoots(webPageService, sourceService);

            boolean rc = false;
            do {
                rc |= process(webPageService.getUnparsedFrontPage(), webPageParserFactory, webPageService);
                webPageService.deDup();
                rc |= process(webPageService.getUnparsedProductList(), webPageParserFactory, webPageService);
                webPageService.deDup();
                rc |= process(webPageService.getUnparsedProductPage(), webPageParserFactory, webPageService);
                webPageService.deDup();
                logger.info("Fetch & parse complete");
                process(webPageService.getParsedProductPageRaw(), webPageService, productService);
                webPageService.deDup();
                indexProducts(productService.getProducts(), elastic, "product" + indexSuffix, "guns");
                productService.markAllAsIndexed();
            } while (rc);
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
        }
    }

    private static void indexProducts(IterableListScrollableResults<ProductEntity> products, Elastic elastic, String index, String type) {
        elastic.index(products, index, type);
    }

    private static void populateRoots(WebPageService webPageService, SourceService sourceService) {
        IterableListScrollableResults<SourceEntity> sources = sourceService.getSources();
        Set<WebPageEntity> newRoots = new HashSet<>();
        Set<SourceEntity> processedSources = new HashSet<>();
        for (SourceEntity sourceEntity : sources) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(sourceEntity.getUrl());
            webPageEntity.setType("frontPage");
            newRoots.add(webPageEntity);
            sourceEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            processedSources.add(sourceEntity);
            logger.info("Adding new root + " + webPageEntity.getUrl());
        }
        webPageService.save(newRoots);
        sourceService.markParsed(processedSources);
    }

    private static boolean process(IterableListScrollableResults<WebPageEntity> parents, WebPageParserFactory webPageParserFactory, WebPageService webPageService) {
        HashSet<WebPageEntity> parsedPages = new HashSet<>();
        boolean rc = false;
        for (WebPageEntity parent : parents) {
            try {
                logger.debug("Start parent page processing: " + parent.getUrl());
                WebPageParser e = webPageParserFactory.getParser(parent);
                Set<WebPageEntity> webPageEntitySet = e.parse(parent);
                if (!webPageEntitySet.isEmpty()) {
                    webPageService.save(webPageEntitySet);
                    rc = true;
                }



                logger.debug("End parent page processing: " + parent.getUrl());
                parsedPages.add(parent);

                if (parsedPages.size() % 100 == 0) {
                    webPageService.markParsed(parsedPages);
                    parsedPages.clear();
                }
            } catch (Exception e) {
                logger.error("Failed to process source " + parent.getUrl(), e);
            }
        }

        webPageService.markParsed(parsedPages);
        parsedPages.clear();
        return rc;
    }

    private static void process(IterableListScrollableResults<WebPageEntity> parents, WebPageService webPageService, ProductService productService) {
        HashSet<WebPageEntity> parsedParents = new HashSet<>();
        HashSet<ProductEntity> products = new HashSet<>();
        ProductParserFactory productParserFactory = new ProductParserFactory();

        for (WebPageEntity parent : parents) {
            ProductParser parser = productParserFactory.getParser(parent);
            if (parser.canParse(parent)) {
                try {
                    products.addAll(parser.parse(parent));
                    parsedParents.add(parent);
                    if (parsedParents.size() % 100 == 0) {
                        webPageService.markParsed(parsedParents);
                        parsedParents.clear();
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse product page " + parent.getUrl(), e);
                }
            }
        }
        productService.save(products);
        webPageService.markParsed(parsedParents);
    }
}
