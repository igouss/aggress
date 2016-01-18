//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.naxsoft.ExecutionContext;
import com.naxsoft.commands.*;
import com.naxsoft.crawler.HttpClientImpl;
import com.naxsoft.database.*;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.util.SslUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;

import static java.lang.System.out;
import static java.lang.System.setProperty;

public class Aggress {
    private static final Logger logger = LoggerFactory.getLogger(Aggress.class);

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final ExecutionContext context = new ExecutionContext();
        final MetricRegistry metrics = new MetricRegistry();
        final Database db = new Database();
        final Elastic elastic = new Elastic();
        final ScheduledReporter elasticReporter = Slf4jReporter.forRegistry(metrics).outputTo(logger).build();
        final OptionSet options = parseCommandLineArguments(args);

        HttpClientImpl asyncFetchClient = null;

        if (!options.hasOptions() || options.has("help")) {
            showHelp();
            return;
        }
        elastic.connect("localhost", 9300);

        context.setOptions(options);
        context.setDb(db);
        context.setMetrics(metrics);
        context.setElastic(elastic);

//            elasticReporter = ElasticsearchReporter.forRegistry(metrics)
//                    .hosts("localhost:9300")
//                    .build();
//            elasticReporter.start(1, TimeUnit.SECONDS);



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

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Install all-trusting host name verifier
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);
            SslUtils instance = SslUtils.getInstance();
            AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
            builder.setAcceptAnyCertificate(true);
            instance.getSSLContext(builder.build()).init(null, trustAllCerts, new java.security.SecureRandom());
            asyncFetchClient = new HttpClientImpl(sc);

            context.setFetchClient(asyncFetchClient);
            context.setProductParserFactory(new ProductParserFactory());

            setProperty("jsse.enableSNIExtension", "false");
            setProperty("jdk.tls.trustNameService", "true");

            context.setWebPageService(new WebPageService(db));
            context.setProductService(new ProductService(db));
            context.setSourceService(new SourceService(db));


            String indexSuffix = "";//"-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            context.setIndexSuffix(indexSuffix);



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

            if (options.has("createESIndex")) {
                CreateESIndexCommand command = new CreateESIndexCommand();
                command.setUp(context);
                command.run();
                command.tearDown();
            }

            if (options.has("createESMappings")) {
                CreateESMappingCommand command = new CreateESMappingCommand();
                command.setUp(context);
                command.run();
                command.tearDown();
            }

            if (options.has("clean")) {
                CleanDBCommand cleanDBCommand = new CleanDBCommand();
                cleanDBCommand.setUp(context);
                cleanDBCommand.run();
                cleanDBCommand.tearDown();
            }

            if (options.has("populate")) {
                PopulateDBCommand populateDBCommand = new PopulateDBCommand();
                populateDBCommand.setUp(context);
                populateDBCommand.run();
                populateDBCommand.tearDown();
            }

            if (options.has("crawl")) {
                CrawlCommand crawlCommand = new CrawlCommand();
                crawlCommand.setUp(context);
                crawlCommand.run();
                crawlCommand.tearDown();
            }

            if (options.has("parse")) {
                ParseCommand parseCommand = new ParseCommand();
                parseCommand.setUp(context);
                parseCommand.run();
                parseCommand.tearDown();
            }
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
            if (null != asyncFetchClient) {
                asyncFetchClient.close();
            }
        }
    }

    private static OptionSet parseCommandLineArguments(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("help");
        parser.accepts("populate");
        parser.accepts("clean");
        parser.accepts("crawl");
        parser.accepts("parse");
        parser.accepts("createESIndex");
        parser.accepts("createESMappings");

        return parser.parse(args);
    }

    private static void showHelp() {
        out.println("Aggress [-populate] [-clean] [-crawl] [-parse] [-createESIndex] [-createESMappings]");
    }
}
