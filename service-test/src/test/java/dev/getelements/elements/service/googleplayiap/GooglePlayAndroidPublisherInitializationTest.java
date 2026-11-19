package dev.getelements.elements.service.googleplayiap;

import org.testng.annotations.Test;

/**
 * Regression coverage for the Google Play verification failure found in 3.9.1 staging (2026-11-18):
 * {@code NoClassDefFoundError: Could not initialize class com.google.api.services.androidpublisher.AndroidPublisher},
 * caused by {@code IllegalStateException: You are currently running with version 2.0.0 of google-api-client.
 * You need at least version 1.15 of google-api-client to run version 1.25.0 of the Google Play Developer API library.}
 *
 * <p>The managed {@code google-api-services-androidpublisher} artifact (v3-rev61-1.25.0) was generated
 * against google-api-client 1.x and its static initializer hard-requires google-api-client major version 1
 * ({@code MAJOR_VERSION == 1 && MINOR_VERSION >= 15}). The platform manages google-api-client 2.0.0, so the
 * class could <b>never</b> initialize: every Google Play verification failed with a 500 on the first call,
 * deterministically, on every environment.
 *
 * <p>This test forces the real class initialization, so the managed androidpublisher artifact and the
 * managed google-api-client must stay mutually compatible. (The service tests mock this class entirely,
 * which is why the incompatibility was invisible to them.)
 */
public class GooglePlayAndroidPublisherInitializationTest {

    @Test
    public void androidPublisherClassInitializesAgainstTheManagedGoogleApiClient() throws Exception {
        Class.forName("com.google.api.services.androidpublisher.AndroidPublisher");
    }

}
