package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.IapSkuDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.model.iap.IapSkuReward;
import dev.getelements.elements.sdk.service.application.ApplicationConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.List;
import java.util.Map;

import static dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService.APPLE_IAP_SCHEME;
import static dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService.GOOGLE_IAP_SCHEME;
import static dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService.FACEBOOK_IAP_SCHEME;
import static dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService.OCULUS_IAP_SCHEME;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class SuperUserApplicationConfigurationService implements ApplicationConfigurationService {

    private static final Map<Class<? extends ApplicationConfiguration>, String> IAP_SCHEMA_BY_CONFIG_TYPE = Map.of(
            IosApplicationConfiguration.class, APPLE_IAP_SCHEME,
            GooglePlayApplicationConfiguration.class, GOOGLE_IAP_SCHEME,
            FacebookApplicationConfiguration.class, FACEBOOK_IAP_SCHEME,
            OculusApplicationConfiguration.class, OCULUS_IAP_SCHEME
    );

    @Inject
    private ApplicationConfigurationDao applicationConfigurationDao;

    @Inject
    private Provider<Transaction> transactionProvider;

    @Override
    public Pagination<ApplicationConfiguration> getApplicationProfiles(String applicationNameOrId, int offset, int count) {
        return applicationConfigurationDao.getActiveApplicationConfigurations(applicationNameOrId, offset, count);
    }

    @Override
    public Pagination<ApplicationConfiguration> getApplicationProfiles(String applicationNameOrId,
                                                                       int offset, int count, String search) {
        return applicationConfigurationDao.getActiveApplicationConfigurations(applicationNameOrId, offset, count, search);
    }

    @Override
    public <T extends ApplicationConfiguration>
    T updateProductBundles(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final Class<T> configurationClass,
            final List<ProductBundle> productBundles) {

        final var result = applicationConfigurationDao.updateProductBundles(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                configurationClass,
                productBundles
        );

        final var schema = IAP_SCHEMA_BY_CONFIG_TYPE.get(configurationClass);

        if (schema != null && productBundles != null) {
            for (final var bundle : productBundles) {
                syncIapSku(schema, bundle);
            }
        }

        return result;
    }

    private void syncIapSku(final String schema, final ProductBundle bundle) {
        final var sku = buildIapSku(schema, bundle);
        try {
            getTransactionProvider().get().performAndCloseV(tx -> tx.getDao(IapSkuDao.class).createIapSku(sku));
        } catch (DuplicateException e) {
            getTransactionProvider().get().performAndCloseV(tx -> {
                final var dao = tx.getDao(IapSkuDao.class);
                final var existing = dao.getIapSku(schema, bundle.getProductId());
                dao.updateIapSku(sku.withId(existing.id()));
            });
        }
    }

    private IapSku buildIapSku(final String schema, final ProductBundle bundle) {
        return new IapSku(
                null,
                schema,
                bundle.getProductId(),
                bundle.getProductBundleRewards().stream()
                        .map(r -> new IapSkuReward(r.getItemId(), r.getQuantity()))
                        .toList());
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(final Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
