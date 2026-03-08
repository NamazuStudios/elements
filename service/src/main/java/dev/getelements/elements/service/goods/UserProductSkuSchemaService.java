package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ProductSkuSchemaDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;
import dev.getelements.elements.sdk.service.goods.ProductSkuSchemaService;
import jakarta.inject.Inject;

public class UserProductSkuSchemaService implements ProductSkuSchemaService {

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
        throw new ForbiddenException("Unprivileged requests cannot create Product SKU Schema records.");
    }

    @Override
    public ProductSkuSchema ensureProductSkuSchema(final String schema) {
        throw new ForbiddenException("Unprivileged requests cannot modify Product SKU Schema records.");
    }

    @Override
    public void deleteProductSkuSchema(final String id) {
        throw new ForbiddenException("Unprivileged requests cannot delete Product SKU Schema records.");
    }

    public ProductSkuSchemaDao getProductSkuSchemaDao() {
        return productSkuSchemaDao;
    }

    @Inject
    public void setProductSkuSchemaDao(ProductSkuSchemaDao productSkuSchemaDao) {
        this.productSkuSchemaDao = productSkuSchemaDao;
    }

}
