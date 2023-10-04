package io.harness.cfsdk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import okhttp3.OkHttpClient;

public class TlsUtils {

    private static final Logger log = LoggerFactory.getLogger(TlsUtils.class);

    public static void setupTls(ApiClient apiClient, CfConfiguration config) {
        if (config == null) {
            return;
        }

        final List<X509Certificate> trustedCAs = config.getTlsTrustedCAs();
        if (trustedCAs != null && !trustedCAs.isEmpty()) {

            final ByteArrayOutputStream certOutputStream = new ByteArrayOutputStream();
            for (X509Certificate cert : trustedCAs) {
                final byte[] bytes = certToByteArray(cert);
                certOutputStream.write(bytes, 0, bytes.length);
            }

            apiClient.setSslCaCert(new ByteArrayInputStream(certOutputStream.toByteArray()));
        }
    }

    public static void setupTls(OkHttpClient.Builder httpClientBuilder, List<X509Certificate> trustedCAs) {

        try {
            if (trustedCAs != null && !trustedCAs.isEmpty()) {

                final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                for (int i = 0; i < trustedCAs.size(); i++) {
                    keyStore.setCertificateEntry("ca" + i, trustedCAs.get(i));
                }

                final TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagers, new SecureRandom());

                httpClientBuilder.sslSocketFactory(
                        sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
            }
        } catch (GeneralSecurityException | IOException ex) {
            String msg = "Failed to setup TLS on SSE endpoint: " + ex.getMessage();
            log.warn("Failed to setup TLS on SSE endpoint: {}", msg);
            throw new RuntimeException(msg, ex);
        }
    }

    private static byte[] certToByteArray(X509Certificate cert) {
        try {
            return cert.getEncoded();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
