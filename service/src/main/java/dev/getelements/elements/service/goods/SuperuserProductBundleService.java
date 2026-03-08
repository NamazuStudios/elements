package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ProductBundleDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.ProductBundle;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.List;

public class SuperuserProductBundleService implements ProductBundleService {

    private ProductBundleDao productBundleDao;

    private Provider<UserProductBundleService> userProductBundleServiceProvider;

    @Override
    public Pagination<ProductBundle> getProductBundles(final int offset, final int count) {
        return getProductBundleDao().getProductBundles(offset, count);
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(final String applicationNameOrId, final int offset, final int count) {
        return getProductBundleDao().getProductBundles(applicationNameOrId, offset, count);
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(final String applicationNameOrId, final String schema,
                                                       final int offset, final int count) {
        return getProductBundleDao().getProductBundles(applicationNameOrId, schema, offset, count);
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(final String applicationNameOrId, final String schema,
                                                       final String productId, final List<String> tags,
                                                       final int offset, final int count) {
        return getProductBundleDao().getProductBundles(applicationNameOrId, schema, productId, tags, offset, count);
    }

    @Override
    public Pagination<ProductBundle> getProductBundlesByTag(final String tag, final int offset, final int count) {
        return getProductBundleDao().getProductBundlesByTag(tag, offset, count);
    }

    @Override
    public ProductBundle getProductBundle(final String id) {
        return getProductBundleDao().getProductBundle(id);
    }

    @Override
    public ProductBundle getProductBundle(final String applicationNameOrId, final String schema, final String productId) {
        return getProductBundleDao().getProductBundle(applicationNameOrId, schema, productId);
    }

    @Override
    public ProductBundle createProductBundle(final ProductBundle bundle) {
        return getProductBundleDao().createProductBundle(bundle);
    }

    @Override
    public ProductBundle updateProductBundle(final ProductBundle bundle) {
        return getProductBundleDao().updateProductBundle(bundle);
    }

    @Override
    public void deleteProductBundle(final String id) {
        getProductBundleDao().deleteProductBundle(id);
    }

    @Override
    public void processVerifiedPurchase(final String schema, final String productId, final String originalTransactionId) {
        getUserProductBundleServiceProvider().get().processVerifiedPurchase(schema, productId, originalTransactionId);
    }

    public ProductBundleDao getProductBundleDao() {
        return productBundleDao;
    }

    @Inject
    public void setProductBundleDao(ProductBundleDao productBundleDao) {
        this.productBundleDao = productBundleDao;
    }

    public Provider<UserProductBundleService> getUserProductBundleServiceProvider() {
        return userProductBundleServiceProvider;
    }

    @Inject
    public void setUserProductBundleServiceProvider(Provider<UserProductBundleService> userProductBundleServiceProvider) {
        this.userProductBundleServiceProvider = userProductBundleServiceProvider;
    }

}
