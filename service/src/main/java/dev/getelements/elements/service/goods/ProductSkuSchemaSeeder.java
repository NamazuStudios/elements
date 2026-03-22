package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ProductSkuSchemaDao;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService;
import dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService;
import dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService;
import dev.getelements.elements.sdk.service.steam.SteamIapReceiptService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductSkuSchemaSeeder {

    private static final Logger logger = LoggerFactory.getLogger(ProductSkuSchemaSeeder.class);

    private ProductSkuSchemaDao productSkuSchemaDao;

    @Inject
    public ProductSkuSchemaSeeder(final ProductSkuSchemaDao productSkuSchemaDao) {
        this.productSkuSchemaDao = productSkuSchemaDao;
        seed();
    }

    private void seed() {
        ensure(AppleIapReceiptService.APPLE_IAP_SCHEME);
        ensure(GooglePlayIapReceiptService.GOOGLE_IAP_SCHEME);
        ensure(OculusIapReceiptService.OCULUS_IAP_SCHEME);
        ensure(FacebookIapReceiptService.FACEBOOK_IAP_SCHEME);
        ensure(SteamIapReceiptService.STEAM_IAP_SCHEME);
    }

    private void ensure(final String schema) {
        final var result = productSkuSchemaDao.ensureProductSkuSchema(schema);
        logger.info("Ensured Product SKU Schema: {} (id={})", result.schema(), result.id());
    }

    public ProductSkuSchemaDao getProductSkuSchemaDao() {
        return productSkuSchemaDao;
    }

    @Inject
    public void setProductSkuSchemaDao(ProductSkuSchemaDao productSkuSchemaDao) {
        this.productSkuSchemaDao = productSkuSchemaDao;
    }

}
