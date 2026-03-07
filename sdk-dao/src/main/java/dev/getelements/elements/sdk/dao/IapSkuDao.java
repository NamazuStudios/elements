package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.iap.IapSku;

@ElementServiceExport
public interface IapSkuDao {

    /**
     * Gets an {@link IapSku} by its database id.
     *
     * @param id the database id
     * @return the {@link IapSku}
     * @throws NotFoundException if not found
     */
    IapSku getIapSku(String id);

    /**
     * Gets an {@link IapSku} by its schema and productId.
     *
     * @param schema    the IAP provider schema
     * @param productId the product id
     * @return the {@link IapSku}
     * @throws NotFoundException if not found
     */
    IapSku getIapSku(String schema, String productId);

    /**
     * Returns all {@link IapSku} records within the given range.
     *
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link IapSku} instances
     */
    Pagination<IapSku> getIapSkus(int offset, int count);

    /**
     * Returns all {@link IapSku} records for a given schema.
     *
     * @param schema the IAP provider schema
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link IapSku} instances
     */
    Pagination<IapSku> getIapSkus(String schema, int offset, int count);

    /**
     * Creates a new {@link IapSku}.
     *
     * @param iapSku the IAP SKU to create
     * @return the created {@link IapSku} as it exists in the database
     * @throws DuplicateException if an IAP SKU with the same schema and productId already exists
     */
    IapSku createIapSku(IapSku iapSku);

    /**
     * Updates an existing {@link IapSku}.
     *
     * @param iapSku the IAP SKU to update
     * @return the updated {@link IapSku} as it exists in the database
     */
    IapSku updateIapSku(IapSku iapSku);

    /**
     * Deletes an {@link IapSku} by its database id.
     *
     * @param id the database id
     * @throws NotFoundException if not found
     */
    void deleteIapSku(String id);

}
