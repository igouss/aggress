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
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Aggress {
    private static final Logger logger = LoggerFactory.getLogger(Aggress.class);

    private static final MetricRegistry metrics = new MetricRegistry();
    private static final Database db = new Database();
    private static Elastic elastic;
    private static ScheduledReporter elasticReporter;
    private static WebPageParserFactory webPageParserFactory;
    private static WebPageService webPageService;
    private static ProductService productService;
    private static SourceService sourceService;
    private static ProductParserFactory productParserFactory;
    private final static int scaleFactor = 1;

    public static void main(String[] args) throws IOException {
        elastic = new Elastic("localhost", 9300);
            elasticReporter = Slf4jReporter.forRegistry(metrics).outputTo(logger)
                .build();
//            elasticReporter = ElasticsearchReporter.forRegistry(metrics)
//                    .hosts("localhost:9300")
//                    .build();
//            elasticReporter.start(1, TimeUnit.SECONDS);

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
            productParserFactory = new ProductParserFactory();
            webPageParserFactory = new WebPageParserFactory(asyncFetchClient);
            System.setProperty("jsse.enableSNIExtension", "false");
            System.setProperty("jdk.tls.trustNameService", "true");

            webPageService = new WebPageService(db);
            productService = new ProductService(db);
            sourceService = new SourceService(db);


            String indexSuffix = "";//"-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            elastic.createIndex(asyncFetchClient, "product", "guns", indexSuffix)
                    .retry(3)
                    .doOnError(ex -> logger.error("Exception", ex))
                    .subscribe();
            elastic.createMapping(asyncFetchClient, "product", "guns", indexSuffix)
                    .retry(3)
                    .doOnError(ex -> logger.error("Exception", ex))
                    .subscribe();

            metrics.register(MetricRegistry.name(Database.class, "web_pages", "unparsed"), (Gauge<Long>) () -> {
                Long rc = -1L;
                StatelessSession session = null;
                try {
                    session = db.getSessionFactory().openStatelessSession();
                    Query query = session.createQuery("select count(id) from WebPageEntity where parsed = false");
                    rc = (Long) query.uniqueResult();
                } catch (Exception e) {
                    logger.error("Metric failed: com.naxsoft.database.Database.web_pages.unparsed", e);
                } finally {
                    if (null != session) {
                        session.close();
                    }
                }
                return rc;
            });
//
            deleteOldData();
            populateSources();
            populateRoots();
            process(webPageService.getUnparsedFrontPage());
            process(webPageService.getUnparsedProductList());
            process(webPageService.getUnparsedProductPage());
            logger.info("Fetch & parse complete");

            processProducts(webPageService.getUnparsedProductPageRaw());
            indexProducts(productService.getProducts(), "product" + indexSuffix, "guns");
            productService.markAllAsIndexed();

            logger.info("Parsing complete");
        } catch (Exception e) {
            logger.error("Application failure", e);
        } finally {
            if (null != elasticReporter) {
                elasticReporter.stop();
            }
            if (null != db) {
                db.close();
            }
            if (null != elastic) {
                elastic.close();
            }
        }
    }


    private static void indexProducts(Observable<ProductEntity> products, String index, String type) {
        elastic.index(products, index, type);
    }


    private static void populateSources() {
        String[] sources = {
                "http://www.alflahertys.com/",
                "http://www.bullseyelondon.com/",
                "http://www.cabelas.ca/",
                "https://www.canadaammo.com/",
                "http://www.canadiangunnutz.com/",
                "https://www.corwin-arms.com/",
                "http://www.crafm.com/",
                "http://ctcsupplies.ca/",
                "https://shop.dantesports.com/",
                "https://ellwoodepps.com/",
                "https://www.irunguns.us/",
                "http://www.marstar.ca/",
                "http://www.sail.ca/",
                "http://www.theammosource.com/",
                "https://www.tradeexcanada.com/",
                "http://westrifle.com/",
                "http://www.wholesalesports.com/",
                "https://www.wolverinesupplies.com/",
                "http://www.firearmsoutletcanada.com/",
                "http://frontierfirearms.ca/",
        };

        Observable.from(sources).map(Aggress::from)
                .retry(3)
                .doOnError(ex -> logger.error("Exception", ex))
                .subscribe(Aggress::save);
    }


    private static void populateRoots() {
        Observable<SourceEntity> sources = sourceService.getSources();
        sources.map(Aggress::from)
                .toList()
                .doOnError(ex -> logger.error("Exception", ex))
                .subscribe(Aggress::save);
        sourceService.markParsed(sources);
    }

    private static SourceEntity from(String sourceUrl) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setEnabled(true);
        sourceEntity.setUrl(sourceUrl);
        return sourceEntity;
    }


    private static void deleteOldData() {
        String[] tables = {
                "SourceEntity",
                "WebPageEntity",
                "ProductEntity"
        };
        hqlTruncate(tables);
    }

    public static void hqlTruncate(String[] tables){
        StatelessSession statelessSession = db.getSessionFactory().openStatelessSession();
        Transaction tx = statelessSession.beginTransaction();
        for(String table : tables) {
            String hql = String.format("delete from %s", table);
            Query query = statelessSession.createQuery(hql);
            query.executeUpdate();
        }
        tx.commit();
        statelessSession.close();
    }

    private static WebPageEntity from(SourceEntity sourceEntity) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(sourceEntity.getUrl());
        webPageEntity.setType("frontPage");
        logger.info("Adding new root {}", webPageEntity.getUrl());
        return webPageEntity;
    }

    private static boolean save(SourceEntity sourceEntity) {
        boolean rc = sourceService.save(sourceEntity);
        if (!rc) {
            logger.error("Failed to save sourceEntity");
        }
        return rc;
    }

    private static boolean save(Collection<WebPageEntity> webPageEntities) {
        boolean rc = webPageService.save(webPageEntities);
        if (!rc) {
            logger.error("Failed to save webPageEntities");
        }
        return rc;
    }

    private static void process(Observable<WebPageEntity> parents) {
        parents.flatMap(parent -> {
            WebPageParser parser = webPageParserFactory.getParser(parent);
            Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
            Timer.Context time = parseTime.time();
            Observable<Set<WebPageEntity>> result = null;
            try {
                result = parser.parse(parent);
            } catch (Exception e) {
                logger.error("Failed to process source {}", parent.getUrl(), e);
            }
            time.stop();
            webPageService.markParsed(parent);
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .map(webPageService::save)
                .retry(3)
                .doOnError(ex -> logger.error("Exception", ex))
                .subscribe();
    }

    private static void processProducts(Observable<WebPageEntity> webPage) {
        webPage.map(webPageEntity -> {
            Set<ProductEntity> result = null;
            try {
                ProductParser parser = productParserFactory.getParser(webPageEntity);
                Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                Timer.Context time = parseTime.time();
                result = parser.parse(webPageEntity);
                time.stop();
                webPageService.markParsed(webPageEntity);
            } catch (Exception e) {
                logger.error("Failed to parse product page {}", webPageEntity.getUrl(), e);
            }
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .retry(3)
                .doOnError(ex -> logger.error("Exception", ex))
                .subscribe(productService::save);
    }
}
