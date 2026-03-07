package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSku;

@ElementServiceExport
public interface ProductSkuDao {

    /**
     * Gets a {@link ProductSku} by its database id.
     *
     * @param id the database id
     * @return the {@link ProductSku}
     * @throws NotFoundException if not found
     */
    ProductSku getProductSku(String id);

    /**
     * Gets a {@link ProductSku} by its schema and productId.
     *
     * @param schema    the purchase provider schema
     * @param productId the product id
     * @return the {@link ProductSku}
     * @throws NotFoundException if not found
     */
    ProductSku getProductSku(String schema, String productId);

    /**
     * Returns all {@link ProductSku} records within the given range.
     *
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link ProductSku} instances
     */
    Pagination<ProductSku> getProductSkus(int offset, int count);

    /**
     * Returns all {@link ProductSku} records for a given schema.
     *
     * @param schema the purchase provider schema
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link ProductSku} instances
     */
    Pagination<ProductSku> getProductSkus(String schema, int offset, int count);

    /**
     * Creates a new {@link ProductSku}.
     *
     * @param productSku the Product SKU to create
     * @return the created {@link ProductSku} as it exists in the database
     * @throws DuplicateException if a Product SKU with the same schema and productId already exists
     */
    ProductSku createProductSku(ProductSku productSku);

    /**
     * Updates an existing {@link ProductSku}.
     *
     * @param productSku the Product SKU to update
     * @return the updated {@link ProductSku} as it exists in the database
     */
    ProductSku updateProductSku(ProductSku productSku);

    /**
     * Deletes a {@link ProductSku} by its database id.
     *
     * @param id the database id
     * @throws NotFoundException if not found
     */
    void deleteProductSku(String id);

}
