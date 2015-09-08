//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.naxsoft.database.Database;
import com.naxsoft.database.Elasitic;
import com.naxsoft.database.ProductService;
import com.naxsoft.database.SourceService;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParser;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Aggress {
    static Logger logger;

    public Aggress() {
    }

    public static void main(String[] args) {
        Database db = null;
        Elasitic elasitic = null;
        logger = LoggerFactory.getLogger(Aggress.class);

        try {
            WebPageParserFactory e = new WebPageParserFactory();
            ProductParserFactory productParserFactory = new ProductParserFactory();

            try {
                db = new Database();
                db.setUp();
                logger.info("Database initialization complete");
            } catch (Exception var14) {
                logger.error("Failed to initialize database", var14);
                if(null != db) {
                    db.tearDown();
                }
            }

            try {
                elasitic = new Elasitic();
                elasitic.setup();
                logger.info("Elastic initialization complete");
            } catch (Exception var13) {
                logger.error("Failed to initialize elastic", var13);
                if(null != elasitic) {
                    elasitic.tearDown();
                }
            }

            WebPageService webPageService = new WebPageService(db);
            ProductService productService = new ProductService(elasitic, db);
            new SourceService(db);
            process(webPageService.getUnparsedProductList(), e, webPageService);
            process(webPageService.getUnparsedProductPage(), e, webPageService);
            logger.info("Fetch & parse complete");
            process(productParserFactory, webPageService, productService);
            logger.info("Parsing complete");
        } catch (Exception var15) {
            logger.error("Application failure", var15);
        } finally {
            if(null != db) {
                db.tearDown();
            }

            if(null != elasitic) {
                db.tearDown();
            }

        }

    }

    private static void process(WebPageParserFactory webPageParserFactory, WebPageService webPageService, SourceService sourceService) {
        Iterator sources = sourceService.getSources();
        HashSet parsedSources = new HashSet();

        while(sources.hasNext()) {
            SourceEntity sourceEntity = (SourceEntity)sources.next();

            try {
                WebPageParser e = webPageParserFactory.getParser(sourceEntity.getUrl(), "frontPage");
                Set webPageEntitySet = e.parse(sourceEntity.getUrl());
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

    private static void process(ProductParserFactory productParserFactory, WebPageService webPageService, ProductService productService) {
        Iterator productPageRaw = webPageService.getUnparsedProductPageRaw();
        HashSet parsedPages = new HashSet();
        HashSet products = new HashSet();

        while(productPageRaw.hasNext()) {
            WebPageEntity webPageEntity = (WebPageEntity)productPageRaw.next();
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

    private static void process(Iterator<WebPageEntity> chain, WebPageParserFactory webPageParserFactory, WebPageService webPageService) {
        HashSet parsedPages = new HashSet();
        boolean i = false;

        while(chain.hasNext()) {
            WebPageEntity webPageEntity = (WebPageEntity)chain.next();
            WebPageParser webPageParser = webPageParserFactory.getParser(webPageEntity.getUrl(), webPageEntity.getType());

            try {
                if(webPageParser.canParse(webPageEntity.getUrl(), webPageEntity.getType())) {
                    Set e = webPageParser.parse(webPageEntity.getUrl());
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
