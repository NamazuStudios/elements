package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;

@ElementServiceExport
@ElementEventProducer(
        value = ProductSkuSchemaDao.PRODUCT_SKU_SCHEMA_CREATED,
        parameters = ProductSkuSchema.class,
        description = "Called when a new product SKU schema is created."
)
@ElementEventProducer(
        value = ProductSkuSchemaDao.PRODUCT_SKU_SCHEMA_CREATED,
        parameters = {ProductSkuSchema.class, Transaction.class},
        description = "Called when a new product SKU schema is created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = ProductSkuSchemaDao.PRODUCT_SKU_SCHEMA_DELETED,
        parameters = ProductSkuSchema.class,
        description = "Called when a product SKU schema is deleted."
)
@ElementEventProducer(
        value = ProductSkuSchemaDao.PRODUCT_SKU_SCHEMA_DELETED,
        parameters = {ProductSkuSchema.class, Transaction.class},
        description = "Called when a product SKU schema is deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface ProductSkuSchemaDao {

    String PRODUCT_SKU_SCHEMA_CREATED = "dev.getelements.elements.sdk.model.dao.productskuschema.created";

    String PRODUCT_SKU_SCHEMA_DELETED = "dev.getelements.elements.sdk.model.dao.productskuschema.deleted";

    /**
     * Returns all {@link ProductSkuSchema} records within the given range.
     *
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link ProductSkuSchema} instances
     */
    Pagination<ProductSkuSchema> getProductSkuSchemas(int offset, int count);

    /**
     * Gets a {@link ProductSkuSchema} by its database id.
     *
     * @param id the database id
     * @return the {@link ProductSkuSchema}
     * @throws NotFoundException if not found
     */
    ProductSkuSchema getProductSkuSchema(String id);

    /**
     * Creates or returns an existing {@link ProductSkuSchema}. If a schema with the same value
     * already exists the existing record is returned, making this call idempotent.
     *
     * @param productSkuSchema the schema to create
     * @return the created or existing {@link ProductSkuSchema}
     */
    ProductSkuSchema createProductSkuSchema(ProductSkuSchema productSkuSchema);

    /**
     * Idempotent upsert — finds an existing schema entry by its string value or creates one.
     * Intended for use by the seeder and payment-provider plugins.
     *
     * @param schema the schema string (e.g. {@code com.apple.appstore})
     * @return the existing or newly-created {@link ProductSkuSchema}
     */
    ProductSkuSchema ensureProductSkuSchema(String schema);

    /**
     * Deletes a {@link ProductSkuSchema} by its database id.
     *
     * @param id the database id
     * @throws NotFoundException if not found
     */
    void deleteProductSkuSchema(String id);

}
