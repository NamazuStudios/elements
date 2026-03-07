package dev.getelements.elements.service.iap;

import dev.getelements.elements.sdk.dao.IapSkuDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.service.iap.IapSkuService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class SuperuserIapSkuService implements IapSkuService {

    private IapSkuDao iapSkuDao;

    private Provider<UserIapSkuService> userIapSkuServiceProvider;

    @Override
    public IapSku getIapSku(final String id) {
        return getIapSkuDao().getIapSku(id);
    }

    @Override
    public IapSku getIapSku(final String schema, final String productId) {
        return getIapSkuDao().getIapSku(schema, productId);
    }

    @Override
    public Pagination<IapSku> getIapSkus(final int offset, final int count) {
        return getIapSkuDao().getIapSkus(offset, count);
    }

    @Override
    public Pagination<IapSku> getIapSkus(final String schema, final int offset, final int count) {
        return getIapSkuDao().getIapSkus(schema, offset, count);
    }

    @Override
    public IapSku createIapSku(final IapSku iapSku) {
        return getIapSkuDao().createIapSku(iapSku);
    }

    @Override
    public IapSku updateIapSku(final IapSku iapSku) {
        return getIapSkuDao().updateIapSku(iapSku);
    }

    @Override
    public void deleteIapSku(final String id) {
        getIapSkuDao().deleteIapSku(id);
    }

    @Override
    public void processVerifiedPurchase(
            final String schema,
            final String productId,
            final String originalTransactionId) {
        getUserIapSkuServiceProvider().get().processVerifiedPurchase(schema, productId, originalTransactionId);
    }

    public IapSkuDao getIapSkuDao() {
        return iapSkuDao;
    }

    @Inject
    public void setIapSkuDao(IapSkuDao iapSkuDao) {
        this.iapSkuDao = iapSkuDao;
    }

    public Provider<UserIapSkuService> getUserIapSkuServiceProvider() {
        return userIapSkuServiceProvider;
    }

    @Inject
    public void setUserIapSkuServiceProvider(Provider<UserIapSkuService> userIapSkuServiceProvider) {
        this.userIapSkuServiceProvider = userIapSkuServiceProvider;
    }

}
