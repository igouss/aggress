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
import rx.schedulers.Schedulers;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AggressRx {
    static Logger logger;

    public AggressRx() {
    }

    public static void main(String[] args) {
        Database db = null;
        Elastic elastic = null;
        logger = LoggerFactory.getLogger(Aggress.class);

        try {


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
                elastic = new Elastic();
                elastic.setup();
                logger.info("Elastic initialization complete");
            } catch (Exception e) {
                logger.error("Failed to initialize elastic", e);
                if (null != elastic) {
                    elastic.tearDown();
                }
            }

            WebPageParserFactory webPageParserFactory = new WebPageParserFactory();
            WebPageService webPageService = new WebPageService(db);
//            ProductService productService = new ProductService(elastic, db);
            SourceService sourceService = new SourceService(db);

//            Observable.interval(0, 1, TimeUnit.SECONDS, Schedulers.computation()).subscribe(t -> {
//                System.out.println(t);
//            });
//            Thread.currentThread().join();

            populateRoots(webPageService, sourceService);

            Observable<WebPageEntity> a = Observable.defer(() -> Observable.from(webPageService.getUnparsedFrontPage()));
            Observable<WebPageEntity> b = Observable.defer(() -> Observable.from(webPageService.getUnparsedProductList()));
            Observable<WebPageEntity> c = Observable.defer(() -> Observable.from(webPageService.getUnparsedFrontPage()));
            Observable<WebPageEntity> d = Observable.defer(() -> Observable.from(webPageService.getUnparsedProductPage()));
            Observable<WebPageEntity> webPageEntityObservable = a.concatWith(b).concatWith(c).concatWith(d);

            webPageEntityObservable.subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).map(webPageEntity -> {
                WebPageParser e = webPageParserFactory.getParser(webPageEntity);
                try {
                    Set<WebPageEntity> webPageEntities = e.parse(webPageEntity);
                    return webPageEntities;
                } catch (Exception e1) {
                    logger.error("Failed to parse " + webPageEntity.getUrl());
                    return null;
                }
            }).filter(webPageEntities -> webPageEntities != null && webPageEntities.size() != 0).map(wpe -> {
                webPageService.save(wpe);
                return wpe.iterator().next().getParent();
            }).toList().subscribe(list -> {
                webPageService.markParsed(list);
            });

            Thread.currentThread().join();


//            process(webPageService.getUnparsedFrontPage(), webPageParserFactory, webPageService);
//            process(webPageService.getUnparsedProductList(), webPageParserFactory, webPageService);
//            process(webPageService.getUnparsedProductPage(), webPageParserFactory, webPageService);
//            logger.info("Fetch & parse complete");
//            process(webPageService.getUnparsedProductPageRaw(), webPageService, productService);
//            logger.info("Parsing complete");
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


    private static void populateRoots(WebPageService webPageService, SourceService sourceService) {
        List<SourceEntity> sources = sourceService.getSources();
        Set<WebPageEntity> newRoots = new HashSet<>();
        for (SourceEntity sourceEntity : sources) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(sourceEntity.getUrl());
            webPageEntity.setType("frontPage");
            newRoots.add(webPageEntity);
            sourceEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        }
        webPageService.save(newRoots);
        sourceService.markParsed(sources);
    }

    private static void process(List<WebPageEntity> parents, WebPageParserFactory webPageParserFactory, WebPageService webPageService) {
        HashSet<WebPageEntity> parsedPages = new HashSet<>();

        for (WebPageEntity parent : parents) {
            try {
                WebPageParser e = webPageParserFactory.getParser(parent);
                Set<WebPageEntity> webPageEntitySet = e.parse(parent);
                if (webPageEntitySet != null) {
                    webPageService.save(webPageEntitySet);
                }
                parsedPages.add(parent);
            } catch (Exception e) {
                logger.error("Failed to process source " + parent.getUrl(), e);
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            }
        }

        webPageService.markParsed(parsedPages);
        parsedPages.clear();
    }

    private static void process(List<WebPageEntity> productPageRaw, WebPageService webPageService, ProductService productService) {
        HashSet<WebPageEntity> parsedPages = new HashSet<>();
        HashSet<ProductEntity> products = new HashSet<>();
        ProductParserFactory productParserFactory = new ProductParserFactory();

        for (WebPageEntity webPageEntity : productPageRaw) {
            ProductParser parser = productParserFactory.getParser(webPageEntity.getUrl(), webPageEntity.getType());
            if (parser.canParse(webPageEntity.getUrl(), webPageEntity.getType())) {
                try {
                    products.addAll(parser.parse(webPageEntity));
                    parsedPages.add(webPageEntity);
                } catch (Exception e) {
                    logger.error("Failed to parse product page " + webPageEntity.getUrl(), e);
                }
            }
        }
        productService.save(products);
        webPageService.markParsed(parsedPages);
        parsedPages.clear();
    }

}
