package dev.getelements.elements.sdk.service.goods;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSku;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ProductSkuService {

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
     * @return the created {@link ProductSku}
     * @throws DuplicateException if a Product SKU with the same schema and productId already exists
     */
    ProductSku createProductSku(ProductSku productSku);

    /**
     * Updates an existing {@link ProductSku}.
     *
     * @param productSku the Product SKU to update
     * @return the updated {@link ProductSku}
     */
    ProductSku updateProductSku(ProductSku productSku);

    /**
     * Deletes a {@link ProductSku} by its database id.
     *
     * @param id the database id
     */
    void deleteProductSku(String id);

    /**
     * Looks up the {@link ProductSku} matching the given provider {@code schema} and {@code productId},
     * and issues a {@link dev.getelements.elements.sdk.model.reward.RewardIssuance} for each
     * configured reward to the current user. Uses
     * {@link dev.getelements.elements.sdk.dao.RewardIssuanceDao#getOrCreateRewardIssuance} so the
     * operation is idempotent — repeated calls with the same arguments produce no duplicate rewards.
     *
     * <p>If no matching {@link ProductSku} is found the call silently returns; this allows providers to
     * coexist with the product-bundle reward system without requiring every SKU to be mapped.
     *
     * @param schema                 the purchase provider schema (e.g. {@code com.apple.appstore})
     * @param productId              the product/SKU identifier as used by the provider
     * @param originalTransactionId  the unique transaction id used to build idempotency keys
     */
    void processVerifiedPurchase(String schema, String productId, String originalTransactionId);

}
