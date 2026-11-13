package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import org.mapstruct.Mapper;
import com.google.api.services.androidpublisher.model.ProductPurchase;

/**
 * Maps the {@link ProductPurchase} returned by the Google Play AndroidPublisher API into the
 * {@link GooglePlayIapReceipt} model persisted by {@code UserGooglePlayIapReceiptService}.
 *
 * <p>The {@code user}, {@code purchaseToken} and {@code productId} fields are set by the service
 * itself ({@code productId} is a path parameter of the purchases().products().get() call, not a
 * field of ProductPurchase) and are intentionally not mapped here.
 */
@Mapper
@ElementPrivate
public interface GooglePlayIapReceiptMapper {

    GooglePlayIapReceipt toGooglePlayIapReceipt(ProductPurchase productPurchase);

}
