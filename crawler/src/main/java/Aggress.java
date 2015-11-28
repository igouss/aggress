//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.codahale.metrics.*;
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
import com.ning.http.util.SslUtils;
import org.elasticsearch.metrics.ElasticsearchReporter;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Aggress {
    private static final Logger logger = LoggerFactory.getLogger(Aggress.class);

    private static final MetricRegistry metrics = new MetricRegistry();
    private static final Database db = new Database();
    private static ScheduledReporter reporter;
    private static WebPageParserFactory webPageParserFactory;
    private static WebPageService webPageService;
    private static ProductService productService;
    private static SourceService sourceService;
    private static Elastic elastic = new Elastic("localhost", 9300);
    private static ProductParserFactory productParserFactory;

    public static void main(String[] args) {
        try {
//        reporter = Slf4jReporter.forRegistry(metrics).outputTo(logger)
//                .build();

            reporter = ElasticsearchReporter.forRegistry(metrics)
//                    .hosts("localhost:9300")
                    .build();
            reporter.start(1, TimeUnit.SECONDS);

        } catch (IOException e) {
            logger.error("Failed to initialize metrics reporter", e);
            return;
        }

        SSLContext sc;
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
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
            SslUtils instance = SslUtils.getInstance();
            AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
            builder.setAcceptAnyCertificate(true);
            instance.getSSLContext(builder.build()).init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (java.security.GeneralSecurityException e) {
            logger.error("Failed to initialize trust manager", e);
            return;
        }

        try (AsyncFetchClient asyncFetchClient = new AsyncFetchClient(sc)) {
            productParserFactory = new ProductParserFactory(metrics);
            webPageParserFactory = new WebPageParserFactory(asyncFetchClient, metrics);
            System.setProperty("jsse.enableSNIExtension", "false");
            System.setProperty("jdk.tls.trustNameService", "true");

            webPageService = new WebPageService(db);
            productService = new ProductService(db);
            sourceService = new SourceService(db);

            String indexSuffix = "";//"-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            elastic.createIndex(asyncFetchClient, "product", "guns", indexSuffix).subscribe();
            elastic.createMapping(asyncFetchClient, "product", "guns", indexSuffix).subscribe();

            metrics.register(MetricRegistry.name(Database.class, "web_pages", "unparsed"), (Gauge<Long>) () -> {
                Long rc = -1L;
                try {
                    StatelessSession session = db.getSessionFactory().openStatelessSession();
                    Query query = session.createQuery("select count(id) from WebPageEntity where parsed = false");
                    rc = (Long) query.uniqueResult();
                    session.close();
                } catch (Exception e) {
                    logger.error("Metric failed: com.naxsoft.database.Database.web_pages.unparsed", e);
                }
                return rc;
            });


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
//            webPageService.deDup();
            indexProducts(productService.getProducts(), "product" + indexSuffix, "guns");
            productService.markAllAsIndexed();

            logger.info("Parsing complete");
        } catch (Exception e) {
            logger.error("Application failure", e);
        } finally {
            if (null != reporter) {
                reporter.stop();
            }
            if (null != db) {
                db.close();
            }
            if (null != elastic) {
                elastic.close();
            }
        }
    }

//    private static Observable<WebPageEntity> parseObservable(Observable<WebPageEntity> webPageEntity) {
//        return webPageEntity.flatMap(Aggress::parseObservableHelper).filter(result -> null != result);
//    }
//
//    private static Observable<Set<ProductEntity>> productFromRawPage(Observable<WebPageEntity> productPageRaw) {
//        return productPageRaw.map(Aggress::productFromRawPageHelper).filter(result -> null != result);
//    }
//
//    private static Observable<WebPageEntity> parseObservableHelper(WebPageEntity webPageEntity) {
//        try {
//            return webPageParserFactory.parse(webPageEntity).flatMap(Observable::from);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private static Set<ProductEntity> productFromRawPageHelper(WebPageEntity webPageEntity) {
//            try {
//                return productParserFactory.parse(webPageEntity);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//    }
//
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
        logger.info("Adding new root {}", webPageEntity.getUrl());
        return webPageEntity;
    }

    private static void save(Collection<WebPageEntity> webPageEntities) {
        webPageService.save(webPageEntities);
    }

    private static void process(Observable<WebPageEntity> parents) {
//        parents.map(parent -> {
//            Observable<Set<WebPageEntity>> parsed = webPageParserFactory.parse(parent);
//            webPageService.markParsed(parent);
//            return parsed;
//        }).flatMap(pages -> webPageService::save).subscribe();

        parents.flatMap(parent -> {
            Observable<Set<WebPageEntity>> parsed = null;
            try {
                parsed = webPageParserFactory.parse(parent);
            } catch (Exception e) {
                logger.error("Failed to process source {}", parent.getUrl(), e);
            }
            webPageService.markParsed(parent);
            return parsed;
        }).filter(webPageEntities -> null != webPageEntities)
                .map(webPageEntities -> webPageService.save(webPageEntities))
                .subscribe();

//        parents.subscribe(parent -> {
//            try {
//                Observable<Set<WebPageEntity>> parsed = webPageParserFactory.parse(parent);
//                webPageService.markParsed(parent);
//
//                parsed.subscribe(webPageService::save, e -> {
//                    logger.error("Failed to save web-page {}", parent.getUrl(), e);
//                });
//            } catch (Exception e) {
//                logger.error("Failed to process source {}", parent.getUrl(), e);
//            }
//        });
    }

    private static void processProducts(Observable<WebPageEntity> webPage) {
        webPage.map(webPageEntity -> {
            Set<ProductEntity> result = new HashSet<>();
            try {
                result.addAll(productParserFactory.parse(webPageEntity));
                webPageService.markParsed(webPageEntity);

            } catch (Exception e) {
                logger.error("Failed to parse product page {}", webPageEntity.getUrl(), e);
            }
            return result;
        }).subscribe(productService::save); // filter(data -> !data.isEmpty()).flatMap(Observable::from).toList()
    }
}
