package dev.getelements.elements.service.receipt;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Regression test: the Google Play receipt-upload flow maps Google's ProductPurchase (returned by
 * the AndroidPublisher API) into GooglePlayIapReceipt through the real MapperRegistry. The required
 * mapping was never registered, so every purchase upload failed with "No mapping exists from
 * ProductPurchase to GooglePlayIapReceipt" -- the Android twin of the Apple receipt-upload failure.
 */
public class GooglePlayIapReceiptMapperTest {

    private MapperRegistry mapperRegistry;

    @BeforeMethod
    public void setUp() {

        final var provider = new ServicesMapperRegistryProvider();
        provider.setApiOutsideUrlProvider(() -> "http://localhost:8080/api/rest");

        mapperRegistry = provider.get();

    }

    @Test
    public void productPurchaseMapsToGooglePlayIapReceipt() {

        final var productPurchase = new ProductPurchase();
        productPurchase.setOrderId("GPA.3300-1234-5678-90123");
        productPurchase.setKind("androidcontent#productPurchase");
        productPurchase.setPurchaseState(0);
        productPurchase.setConsumptionState(0);
        productPurchase.setDeveloperPayload("payload-1");
        productPurchase.setPurchaseType(0);
        productPurchase.setPurchaseTimeMillis(1690000000000L);

        final var googlePlayIapReceipt = mapperRegistry.map(productPurchase, GooglePlayIapReceipt.class);

        assertNotNull(googlePlayIapReceipt);
        assertEquals(googlePlayIapReceipt.getOrderId(), "GPA.3300-1234-5678-90123");
        assertEquals(googlePlayIapReceipt.getKind(), "androidcontent#productPurchase");
        assertEquals(googlePlayIapReceipt.getPurchaseState(), Integer.valueOf(0));
        assertEquals(googlePlayIapReceipt.getConsumptionState(), Integer.valueOf(0));
        assertEquals(googlePlayIapReceipt.getDeveloperPayload(), "payload-1");
        assertEquals(googlePlayIapReceipt.getPurchaseType(), Integer.valueOf(0));
        assertEquals(googlePlayIapReceipt.getPurchaseTimeMillis(), Long.valueOf(1690000000000L));

    }

}
