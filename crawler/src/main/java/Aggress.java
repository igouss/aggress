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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Aggress {
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

            WebPageService webPageService = new WebPageService(db);
            ProductService productService = new ProductService(elastic, db);
            SourceService sourceService = new SourceService(db);

//            populateRoots(webPageService, sourceService);
//            process(webPageService.getUnparsedFrontPage(), webPageParserFactory, webPageService);
//            process(webPageService.getUnparsedProductList(), webPageParserFactory, webPageService);
//            process(webPageService.getUnparsedProductPage(), webPageParserFactory, webPageService);
//            logger.info("Fetch & parse complete");
//            process(webPageService.getUnparsedProductPageRaw(), webPageService, productService);
//            logger.info("Parsing complete");

            do {
                List<WebPageEntity> unparsedPages = webPageService.getUnparsedPage();
                if (unparsedPages.isEmpty()) {
                    break;
                } else {
                    process(unparsedPages, webPageParserFactory, webPageService);
                }
            } while (true);

//            Thread.currentThread().join();
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

        }

        webPageService.markParsed(parsedPages);
        parsedPages.clear();
    }

    private static void process(List<WebPageEntity> parents, WebPageService webPageService, ProductService productService) {
        HashSet<WebPageEntity> parsedPages = new HashSet<>();
        HashSet<ProductEntity> products = new HashSet<>();
        ProductParserFactory productParserFactory = new ProductParserFactory();

        for (WebPageEntity parent : parents) {
            ProductParser parser = productParserFactory.getParser(parent);
            if (parser.canParse(parent)) {
                try {
                    products.addAll(parser.parse(parent));
                    parsedPages.add(parent);
                } catch (Exception e) {
                    logger.error("Failed to parse product page " + parent.getUrl(), e);
                }
            }
        }
        productService.save(products);
        webPageService.markParsed(parsedPages);
        parsedPages.clear();
    }


}
