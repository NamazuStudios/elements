package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ProductSkuDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.ProductSku;
import dev.getelements.elements.sdk.service.goods.ProductSkuService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class SuperuserProductSkuService implements ProductSkuService {

    private ProductSkuDao productSkuDao;

    private Provider<UserProductSkuService> userProductSkuServiceProvider;

    @Override
    public ProductSku getProductSku(final String id) {
        return getProductSkuDao().getProductSku(id);
    }

    @Override
    public ProductSku getProductSku(final String schema, final String productId) {
        return getProductSkuDao().getProductSku(schema, productId);
    }

    @Override
    public Pagination<ProductSku> getProductSkus(final int offset, final int count) {
        return getProductSkuDao().getProductSkus(offset, count);
    }

    @Override
    public Pagination<ProductSku> getProductSkus(final String schema, final int offset, final int count) {
        return getProductSkuDao().getProductSkus(schema, offset, count);
    }

    @Override
    public ProductSku createProductSku(final ProductSku productSku) {
        return getProductSkuDao().createProductSku(productSku);
    }

    @Override
    public ProductSku updateProductSku(final ProductSku productSku) {
        return getProductSkuDao().updateProductSku(productSku);
    }

    @Override
    public void deleteProductSku(final String id) {
        getProductSkuDao().deleteProductSku(id);
    }

    @Override
    public void processVerifiedPurchase(
            final String schema,
            final String productId,
            final String originalTransactionId) {
        getUserProductSkuServiceProvider().get().processVerifiedPurchase(schema, productId, originalTransactionId);
    }

    public ProductSkuDao getProductSkuDao() {
        return productSkuDao;
    }

    @Inject
    public void setProductSkuDao(ProductSkuDao productSkuDao) {
        this.productSkuDao = productSkuDao;
    }

    public Provider<UserProductSkuService> getUserProductSkuServiceProvider() {
        return userProductSkuServiceProvider;
    }

    @Inject
    public void setUserProductSkuServiceProvider(Provider<UserProductSkuService> userProductSkuServiceProvider) {
        this.userProductSkuServiceProvider = userProductSkuServiceProvider;
    }

}
