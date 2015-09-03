import com.naxsoft.database.Database;
import com.naxsoft.database.Elasitic;
import com.naxsoft.database.ProductService;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.Product;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParser;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 */
public class Aggress {
    public static void main(String[] args) {
        Database db = null;
        Elasitic elasitic = null;

        try {
            Logger logger = LoggerFactory.getLogger(Aggress.class);
            WebPageParserFactory webPageParserFactory = new WebPageParserFactory();
            ProductParserFactory productParserFactory = new ProductParserFactory();
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

//            WebPageService webPageService = new WebPageService(db);
//            ProductService productService = new ProductService(elasitic);
//
//            SourceService sourceService = new SourceService(db);
//            Iterator<SourceEntity> sources = sourceService.getSources();
//            while (sources.hasNext()) {
//                SourceEntity sourceEntity = sources.next();
//                WebPageParser parser = webPageParserFactory.getParser(sourceEntity.getUrl(), "frontPage");
//                Set<WebPageEntity> webPageEntitySet = parser.parse(sourceEntity.getUrl());
//                if (webPageEntitySet != null) {
//                    webPageService.save(sourceEntity, webPageEntitySet);
//                }
//            }
//
//            process(webPageService.getUnparsedProductList(), webPageParserFactory, webPageService);
//            process(webPageService.getUnparsedProductPage(), webPageParserFactory, webPageService);
//            logger.info("Fetch & parse complete");
//            process(productParserFactory, webPageService, productService);
//            logger.info("Parsing complete");

            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            if (null != db) {
                db.tearDown();
            }
            if (null != elasitic) {
                db.tearDown();
            }
        }
    }

    private static void process(ProductParserFactory productParserFactory, WebPageService webPageService, ProductService productService) {
        Iterator<WebPageEntity> productPageRaw = webPageService.getUnparsedProductPageRaw();
        Set<Product> products = new HashSet<>();
        while (productPageRaw.hasNext()) {
            WebPageEntity webPageEntity = productPageRaw.next();
            ProductParser parser = productParserFactory.getParser(webPageEntity.getUrl(), webPageEntity.getType());
            products.addAll(parser.parse(webPageEntity));
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        productService.save(products);
    }

    private static void process(Iterator<WebPageEntity> chain, WebPageParserFactory webPageParserFactory, WebPageService webPageService) {
        Set<WebPageEntity> parsedPages = new HashSet<>();
        int i = 0;
        while (chain.hasNext()) {
            WebPageEntity webPageEntity = chain.next();
            WebPageParser webPageParser = webPageParserFactory.getParser(webPageEntity.getUrl(), webPageEntity.getType());
            Set<WebPageEntity> webPageEntitySet = webPageParser.parse(webPageEntity.getUrl());
            if (webPageEntitySet != null) {
                parsedPages.add(webPageEntity);
                webPageService.save(webPageEntity.getSourceBySourceId(), webPageEntitySet);
                if (++i % 100 == 0) {
                    webPageService.markParsed(parsedPages);
                    parsedPages.clear();
                }
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        webPageService.markParsed(parsedPages);
        parsedPages.clear();
    }
}
