package dev.getelements.elements.sdk.service.goods;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ProductSkuSchemaService {

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
     * Idempotent upsert — finds or creates a {@link ProductSkuSchema} by its string value.
     * Payment-provider plugins should call this at startup to register their schema identifiers.
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
