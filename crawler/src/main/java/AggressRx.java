//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.naxsoft.database.*;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParser;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AggressRx {
    static Logger logger;

    public static void main(String[] args) {
        Database db = null;
        Elasitic elasitic = null;
        logger = LoggerFactory.getLogger(Aggress.class);

        try {
            WebPageParserFactory webPageParserFactory = new WebPageParserFactory();


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

            try {
                elasitic = new Elasitic();
                elasitic.setup();
                logger.info("Elastic initialization complete");
            } catch (Exception e) {
                logger.error("Failed to initialize elastic", e);
                if (null != elasitic) {
                    elasitic.tearDown();
                }
            }

            WebPageService webPageService = new WebPageService(db);
            ProductService productService = new ProductService(elasitic, db);
            SourceService sourceService = new SourceService(db);

            Scheduler subscriptionScheduler = Schedulers.io();
            Scheduler observationScheduler = Schedulers.io();
            Observable
                    .from(sourceService.getSources())
                    .subscribeOn(subscriptionScheduler)
                    .observeOn(observationScheduler)
                    .subscribe(sourceEntity -> {
                        System.out.println(Thread.currentThread().toString());
                        HashSet<SourceEntity> parsedSources = new HashSet<>();
                        try {
                            WebPageParser e = webPageParserFactory.getParser(sourceEntity.getUrl(), "frontPage");
                            Set<WebPageEntity> webPageEntitySet = e.parse(sourceEntity.getUrl());
                            if (webPageEntitySet != null) {
                                webPageService.save(sourceEntity, webPageEntitySet);
                            }

                            parsedSources.add(sourceEntity);
                        } catch (Exception var8) {
                            logger.error("Failed to process source " + sourceEntity.getUrl());
                        }
                        sourceService.markParsed(parsedSources);

                    });


            Observable<SourceEntity> sourceObservable = Observable.from(sourceService.getSources());
            Observable<WebPageEntity> webPageEntityObservable = Observable
                    .from(webPageService.getUnparsedProductList())
                    .mergeWith(Observable.from(webPageService.getUnparsedProductPage()))
                    .mergeWith(Observable.from(webPageService.getUnparsedProductPageRaw()));


            logger.info("Application complete");

            Thread.currentThread().join();

//            process(sourceService.getSources(), webPageParserFactory, webPageService, sourceService);
//            process(webPageService.getUnparsedProductList(), webPageParserFactory, webPageService);
//            process(webPageService.getUnparsedProductPage(), webPageParserFactory, webPageService);
//            process(webPageService.getUnparsedProductPageRaw(), webPageService, productService);
        } catch (Exception var15) {
            logger.error("Application failure", var15);
        } finally {
            if (null != db) {
                db.tearDown();
            }
            if (null != elasitic) {
                elasitic.tearDown();
            }
        }
    }


    private static void process( List<SourceEntity> sources, WebPageParserFactory webPageParserFactory, WebPageService webPageService, SourceService sourceService) {
        HashSet<SourceEntity> parsedSources = new HashSet<>();

        for(SourceEntity sourceEntity : sources) {
            try {
                WebPageParser e = webPageParserFactory.getParser(sourceEntity.getUrl(), "frontPage");
                Set<WebPageEntity> webPageEntitySet = e.parse(sourceEntity.getUrl());
                if(webPageEntitySet != null) {
                    webPageService.save(sourceEntity, webPageEntitySet);
                }

                parsedSources.add(sourceEntity);
            } catch (Exception var8) {
                logger.error("Failed to process source " + sourceEntity.getUrl());
            }
        }

        sourceService.markParsed(parsedSources);
    }

    private static void process(List<WebPageEntity> productPageRaw, WebPageService webPageService, ProductService productService) {
        HashSet<WebPageEntity> parsedPages = new HashSet<>();
        HashSet<ProductEntity> products = new HashSet<>();
        ProductParserFactory productParserFactory = new ProductParserFactory();

        for(WebPageEntity webPageEntity : productPageRaw) {
            ProductParser parser = productParserFactory.getParser(webPageEntity.getUrl(), webPageEntity.getType());
            if(parser.canParse(webPageEntity.getUrl(), webPageEntity.getType())) {
                try {
                    products.addAll(parser.parse(webPageEntity));
                    parsedPages.add(webPageEntity);
                } catch (Exception var9) {
                    logger.error("Failed to parse product page " + webPageEntity.getUrl(), var9);
                }
            }
        }

        productService.save(products);
        webPageService.markParsed(parsedPages);
        parsedPages.clear();
    }

    private static void process(List<WebPageEntity> chain, WebPageParserFactory webPageParserFactory, WebPageService webPageService) {
        HashSet<WebPageEntity> parsedPages = new HashSet<>();

        for(WebPageEntity webPageEntity : chain) {
            WebPageParser webPageParser = webPageParserFactory.getParser(webPageEntity.getUrl(), webPageEntity.getType());

            try {
                if(webPageParser.canParse(webPageEntity.getUrl(), webPageEntity.getType())) {
                    Set<WebPageEntity> e = webPageParser.parse(webPageEntity.getUrl());
                    parsedPages.add(webPageEntity);
                    webPageService.save(webPageEntity.getSourceBySourceId(), e);
                }
            } catch (Exception var9) {
                logger.error("Failed to parse " + webPageEntity.getUrl());
            }

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
            } catch (InterruptedException var8) {
                var8.printStackTrace();
            }
        }

        webPageService.markParsed(parsedPages);
        parsedPages.clear();
    }
}
