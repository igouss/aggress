package com.naxsoft.providers;

import com.naxsoft.commands.CleanDBCommand;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.crawler.HttpClientNing;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.util.SslUtils;
import dagger.Module;
import dagger.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class HttpClientModule {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpClientModule.class);

    @Provides
    @Singleton
    @NotNull
    HttpClient get() {
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

            return new HttpClientNing(sc);
        } catch (GeneralSecurityException e) {
            LOGGER.error("Failed to initialize HttpClientModule", e);
            throw new RuntimeException("Failed to initialize HttpClientModule", e);
        }
    }
}
