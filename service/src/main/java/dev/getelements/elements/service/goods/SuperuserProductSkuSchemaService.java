package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ProductSkuSchemaDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.goods.ProductSkuSchemaService;
import dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService;
import dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService;
import dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService;
import jakarta.inject.Inject;

import java.util.Set;

public class SuperuserProductSkuSchemaService implements ProductSkuSchemaService {

    private static final Set<String> BUILT_IN_SCHEMAS = Set.of(
            AppleIapReceiptService.APPLE_IAP_SCHEME,
            GooglePlayIapReceiptService.GOOGLE_IAP_SCHEME,
            OculusIapReceiptService.OCULUS_IAP_SCHEME,
            FacebookIapReceiptService.FACEBOOK_IAP_SCHEME
    );

    private ProductSkuSchemaDao productSkuSchemaDao;

    @Override
    public Pagination<ProductSkuSchema> getProductSkuSchemas(final int offset, final int count) {
        return getProductSkuSchemaDao().getProductSkuSchemas(offset, count);
    }

    @Override
    public ProductSkuSchema getProductSkuSchema(final String id) {
        return getProductSkuSchemaDao().getProductSkuSchema(id);
    }

    @Override
    public ProductSkuSchema createProductSkuSchema(final ProductSkuSchema productSkuSchema) {
        return getProductSkuSchemaDao().createProductSkuSchema(productSkuSchema);
    }

    @Override
    public ProductSkuSchema ensureProductSkuSchema(final String schema) {
        return getProductSkuSchemaDao().ensureProductSkuSchema(schema);
    }

    @Override
    public void deleteProductSkuSchema(final String id) {

        final var schema = getProductSkuSchemaDao().getProductSkuSchema(id);

        if (BUILT_IN_SCHEMAS.contains(schema.schema())) {
            throw new ForbiddenException("Cannot delete built-in schema: " + schema.schema());
        }

        getProductSkuSchemaDao().deleteProductSkuSchema(id);
    }

    public ProductSkuSchemaDao getProductSkuSchemaDao() {
        return productSkuSchemaDao;
    }

    @Inject
    public void setProductSkuSchemaDao(ProductSkuSchemaDao productSkuSchemaDao) {
        this.productSkuSchemaDao = productSkuSchemaDao;
    }

}
