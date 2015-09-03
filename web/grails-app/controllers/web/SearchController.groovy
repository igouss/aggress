package web

import com.naxsoft.parsers.productParser.ProductParserFactory
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory
import org.slf4j.LoggerFactory

class SearchController {

    def index() {
        def logger = LoggerFactory.getLogger(SearchController.class);
        logger.info("From search controller")
        def webPageParserFactory = new WebPageParserFactory();
        def productParserFactory = new ProductParserFactory();

        render {"Hello World!"}
    }
}
