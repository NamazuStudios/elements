package com.namazustudios.socialengine.service.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;
import com.namazustudios.socialengine.util.AppleDateFormat;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.google.common.io.ByteStreams.toByteArray;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.APPLE_ITUNES;
import static com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.SANDBOX;
import static javax.net.ssl.HttpsURLConnection.*;
import static org.testng.Assert.fail;

@Guice(modules = TestVerifyAppleReceipt.Module.class)
public class TestVerifyAppleReceipt {

    private AppleIapVerifyReceiptInvoker.Builder builder;
    static {
        disableSslVerification();
    }

    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSandboxVerification() throws Exception {
        try (final InputStream is = TestVerifyAppleReceipt.class.getResourceAsStream("/iap_sandbox_receipt.txt")) {

            final String receiptData = new String(toByteArray(is), "UTF-8");

            getBuilder()
                .withEnvironment(SANDBOX)
                .withReceiptData(receiptData)
                .build()
                .invoke();

        }
    }

    public AppleIapVerifyReceiptInvoker.Builder getBuilder() {
        return builder;
    }

    @Inject
    public void setBuilder(AppleIapVerifyReceiptInvoker.Builder builder) {
        this.builder = builder;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(AppleIapVerifyReceiptInvoker.Builder.class).to(DefaultAppleIapVerifyReceiptInvokerBuilder.class);
            install(new JacksonHttpClientModule()
            .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
            .withDefaultObjectMapperProvider(() -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            }).withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setDateFormat(new AppleDateFormat());
                objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            }));
        }

    }

}
