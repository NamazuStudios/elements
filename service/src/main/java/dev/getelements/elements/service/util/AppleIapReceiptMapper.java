package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceiptPurchase;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * Maps Apple's grand-unified receipt (the payload returned by the verifyReceipt endpoint) into the
 * {@link AppleIapReceipt} model persisted by {@code UserAppleIapReceiptService}.
 *
 * <p>The service maps the grand-unified receipt first (bundle and original-purchase metadata), then
 * overlays each in-app purchase record onto the same instance, which fills the transaction-level
 * fields (quantity, productId, originalTransactionId). The {@code user} and {@code receiptData}
 * fields are set by the service itself and are intentionally not mapped here.
 */
@Mapper
@ElementPrivate
public interface AppleIapReceiptMapper {

    AppleIapReceipt toAppleIapReceipt(AppleIapGrandUnifiedReceipt grandUnifiedReceipt);

    void updateAppleIapReceipt(
            AppleIapGrandUnifiedReceiptPurchase grandUnifiedReceiptPurchase,
            @MappingTarget AppleIapReceipt appleIapReceipt);

}
