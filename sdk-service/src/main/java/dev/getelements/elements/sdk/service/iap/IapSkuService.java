package dev.getelements.elements.sdk.service.iap;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.iap.IapSku;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface IapSkuService {

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
     * @return the created {@link IapSku}
     * @throws DuplicateException if an IAP SKU with the same schema and productId already exists
     */
    IapSku createIapSku(IapSku iapSku);

    /**
     * Updates an existing {@link IapSku}.
     *
     * @param iapSku the IAP SKU to update
     * @return the updated {@link IapSku}
     */
    IapSku updateIapSku(IapSku iapSku);

    /**
     * Deletes an {@link IapSku} by its database id.
     *
     * @param id the database id
     */
    void deleteIapSku(String id);

    /**
     * Looks up the {@link IapSku} matching the given provider {@code schema} and {@code productId},
     * and issues a {@link dev.getelements.elements.sdk.model.reward.RewardIssuance} for each
     * configured reward to the current user. Uses
     * {@link dev.getelements.elements.sdk.dao.RewardIssuanceDao#getOrCreateRewardIssuance} so the
     * operation is idempotent — repeated calls with the same arguments produce no duplicate rewards.
     *
     * <p>If no matching {@link IapSku} is found the call silently returns; this allows providers to
     * coexist with the product-bundle reward system without requiring every SKU to be mapped.
     *
     * @param schema                 the IAP provider schema (e.g. {@code com.apple.appstore})
     * @param productId              the product/SKU identifier as used by the provider
     * @param originalTransactionId  the unique transaction id used to build idempotency keys
     */
    void processVerifiedPurchase(String schema, String productId, String originalTransactionId);

}
