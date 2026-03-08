package dev.getelements.elements.sdk.service.goods;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductBundle;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ProductBundleService {

    /**
     * Returns all {@link ProductBundle} records within the given range.
     *
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link ProductBundle} instances
     */
    Pagination<ProductBundle> getProductBundles(int offset, int count);

    /**
     * Returns all {@link ProductBundle} records for a given application.
     *
     * @param applicationNameOrId the application name or id
     * @param offset              the offset
     * @param count               the count
     * @return a {@link Pagination} of {@link ProductBundle} instances
     */
    Pagination<ProductBundle> getProductBundles(String applicationNameOrId, int offset, int count);

    /**
     * Returns all {@link ProductBundle} records for a given application and schema.
     *
     * @param applicationNameOrId the application name or id
     * @param schema              the purchase provider schema
     * @param offset              the offset
     * @param count               the count
     * @return a {@link Pagination} of {@link ProductBundle} instances
     */
    Pagination<ProductBundle> getProductBundles(String applicationNameOrId, String schema, int offset, int count);

    /**
     * Gets a {@link ProductBundle} by its database id.
     *
     * @param id the database id
     * @return the {@link ProductBundle}
     * @throws NotFoundException if not found
     */
    ProductBundle getProductBundle(String id);

    /**
     * Gets a {@link ProductBundle} by application, schema, and productId.
     *
     * @param applicationNameOrId the application name or id
     * @param schema              the purchase provider schema
     * @param productId           the product id
     * @return the {@link ProductBundle}
     * @throws NotFoundException if not found
     */
    ProductBundle getProductBundle(String applicationNameOrId, String schema, String productId);

    /**
     * Creates a new {@link ProductBundle}.
     *
     * @param bundle the Product Bundle to create
     * @return the created {@link ProductBundle}
     * @throws DuplicateException if a bundle with the same application, schema and productId already exists
     */
    ProductBundle createProductBundle(ProductBundle bundle);

    /**
     * Updates an existing {@link ProductBundle}.
     *
     * @param bundle the Product Bundle to update
     * @return the updated {@link ProductBundle}
     */
    ProductBundle updateProductBundle(ProductBundle bundle);

    /**
     * Deletes a {@link ProductBundle} by its database id.
     *
     * @param id the database id
     */
    void deleteProductBundle(String id);

    /**
     * Returns all {@link ProductBundle} records matching any combination of the supplied optional filters.
     * Any parameter that is {@code null} or blank is ignored.
     *
     * @param applicationNameOrId the application name or id, or {@code null}
     * @param schema              the purchase provider schema, or {@code null}
     * @param productId           the product id, or {@code null}
     * @param tags                tags the bundle must contain (all must match), or {@code null}/empty
     * @param offset              the offset
     * @param count               the count
     * @return a {@link Pagination} of matching {@link ProductBundle} instances
     */
    Pagination<ProductBundle> getProductBundles(String applicationNameOrId, String schema,
                                                String productId, List<String> tags, int offset, int count);

    /**
     * Returns all {@link ProductBundle} records that contain the given tag.
     *
     * @param tag    the tag to filter by
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link ProductBundle} instances
     */
    Pagination<ProductBundle> getProductBundlesByTag(String tag, int offset, int count);

    /**
     * Looks up the {@link ProductBundle} matching the given provider {@code schema} and {@code productId}
     * for the current user's application, and issues a reward issuance for each configured reward.
     * Uses idempotency so repeated calls with the same arguments produce no duplicate rewards.
     *
     * <p>If no matching {@link ProductBundle} is found the call silently returns.
     *
     * @param schema                the purchase provider schema (e.g. {@code com.apple.appstore})
     * @param productId             the product/SKU identifier as used by the provider
     * @param originalTransactionId the unique transaction id used to build idempotency keys
     */
    void processVerifiedPurchase(String schema, String productId, String originalTransactionId);

}
