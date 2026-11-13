package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceiptPurchase;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Regression test: the Apple receipt-upload flow maps Apple's grand-unified receipt and its in-app
 * purchase records into AppleIapReceipt through the real MapperRegistry. The required mapping was
 * never registered, so every receipt upload failed with "No mapping exists from
 * AppleIapGrandUnifiedReceipt to AppleIapReceipt" -- previously surfacing as a silent 500.
 */
public class AppleIapReceiptMapperTest {

    private MapperRegistry mapperRegistry;

    @BeforeMethod
    public void setUp() {

        final var provider = new ServicesMapperRegistryProvider();
        provider.setApiOutsideUrlProvider(() -> "http://localhost:8080/api/rest");

        mapperRegistry = provider.get();

    }

    @Test
    public void grandUnifiedReceiptAndPurchaseMapToAppleIapReceipt() {

        final var purchase = new AppleIapGrandUnifiedReceiptPurchase();
        purchase.setQuantity(3);
        purchase.setProductId("com.example.coins");
        purchase.setOriginalTransactionId("1000000123456789");
        purchase.setOriginalPurchaseDate(new Date(1690000000000L));

        final var grandUnifiedReceipt = new AppleIapGrandUnifiedReceipt();
        grandUnifiedReceipt.setBundleId("com.example.app");
        grandUnifiedReceipt.setOriginalPurchaseDate(new Date(1680000000000L));
        grandUnifiedReceipt.setInApp(List.of(purchase));

        final var appleIapReceipt = mapperRegistry.map(grandUnifiedReceipt, AppleIapReceipt.class);

        assertNotNull(appleIapReceipt);
        assertEquals(appleIapReceipt.getBundleId(), "com.example.app");
        assertEquals(appleIapReceipt.getOriginalPurchaseDate(), new Date(1680000000000L));

        // The service overlays each in-app purchase record onto the same instance; the
        // transaction-level fields come from the purchase.
        mapperRegistry.map(purchase, appleIapReceipt);

        assertEquals(appleIapReceipt.getQuantity(), Integer.valueOf(3));
        assertEquals(appleIapReceipt.getProductId(), "com.example.coins");
        assertEquals(appleIapReceipt.getOriginalTransactionId(), "1000000123456789");
        assertEquals(appleIapReceipt.getOriginalPurchaseDate(), new Date(1690000000000L));

    }

}
