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
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.SSLEngineFactory;
import com.ning.http.util.SslUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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
    private static Database db;


    public static void main(String[] args) {
        SSLContext sc = null;
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };
            // Install the all-trusting trust manager

            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Install all-trusting host name verifier
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> {
                return true;
            });
            SslUtils instance = SslUtils.getInstance();
            AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
            builder.setAcceptAnyCertificate(true);
            instance.getSSLContext(builder.build()).init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (java.security.GeneralSecurityException e) {
            e.printStackTrace();
        }
        logger = LoggerFactory.getLogger(Aggress.class);

        try (AsyncFetchClient asyncFetchClient = new AsyncFetchClient(sc)) {
            productParserFactory = new ProductParserFactory();
            webPageParserFactory = new WebPageParserFactory(asyncFetchClient);
            System.setProperty("jsse.enableSNIExtension", "false");
            System.setProperty("jdk.tls.trustNameService", "true");


            try {
                logger.info("Elastic initialization complete");
                elastic = new Elastic();

                try {
                    db = new Database();
                    logger.info("Database initialization complete");
                } catch (Exception e) {
                    logger.error("Failed to initialize database", e);
                    if (null != db) {
                        db.close();
                        return;
                    }
                }

                webPageService = new WebPageService(db);
                productService = new ProductService(elastic, db);
                sourceService = new SourceService(db);

                String indexSuffix = "";//"-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                elastic.createIndex(asyncFetchClient, "product", "guns", indexSuffix).subscribe();
                elastic.createMapping(asyncFetchClient, "product", "guns", indexSuffix).subscribe();


//            webPageService.getUnparsedFrontPage().
//                    map(Aggress::parseObservableHelper).
//                    map(Aggress::parseObservable).
//                    map(Aggress::parseObservable).
//                    map(Aggress::productFromRawPage).
//                    subscribe(products -> products.subscribe(set -> productService.save(set)));

                populateRoots();
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
                logger.error("Failed to initialize elastic", e);
                if (null != elastic) {
                    elastic.close();
                }
            }

        } catch (Exception e) {
            logger.error("Application failure", e);
        } finally {
            if (null != db) {
                db.close();
            }
            if (null != elastic) {
                elastic.close();
            }
        }
    }

//    private static Observable<WebPageEntity> parseObservable(Observable<WebPageEntity> webPageEntity) {
//        return webPageEntity.flatMap(Aggress::parseObservableHelper).filter(result -> result != null);
//    }
//
//    private static Observable<Set<ProductEntity>> productFromRawPage(Observable<WebPageEntity> productPageRaw) {
//        return productPageRaw.map(Aggress::productFromRawPageHelper).filter(result -> result != null);
//    }
//
//    private static Observable<WebPageEntity> parseObservableHelper(WebPageEntity webPageEntity) {
//        try {
//            return webPageParserFactory.getParser(webPageEntity).parse(webPageEntity).flatMap(set -> Observable.from(set));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private static Set<ProductEntity> productFromRawPageHelper(WebPageEntity webPageEntity) {
//            try {
//                return productParserFactory.getParser(webPageEntity).parse(webPageEntity);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//    }

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
        parents.subscribe(parent -> {
            try {
                WebPageParser parser = webPageParserFactory.getParser(parent);
                Observable<Set<WebPageEntity>> parsed = parser.parse(parent);
                webPageService.markParsed(parent);

                parsed.subscribe((webPageEntitySet) -> {
                    webPageService.save(webPageEntitySet);
                }, e -> {
                    logger.error("Failed to save web-page " + parent.getUrl(), e);
                });
            } catch (Exception e) {
                logger.error("Failed to process source " + parent.getUrl(), e);
            }
        });
    }

    private static void processProducts(Observable<WebPageEntity> webPage) {
        webPage.map(webPageEntity -> {
            ProductParser parser = productParserFactory.getParser(webPageEntity);
            Set<ProductEntity> result = new HashSet<>();
            try {
                result.addAll(parser.parse(webPageEntity));
                webPageService.markParsed(webPageEntity);

            } catch (Exception e) {
                logger.error("Failed to parse product page " + webPageEntity.getUrl(), e);
            }
            return result;
        }).subscribe((products) -> {
            productService.save(products);
        }); // filter(data -> !data.isEmpty()).flatMap(Observable::from).toList()
    }
}
