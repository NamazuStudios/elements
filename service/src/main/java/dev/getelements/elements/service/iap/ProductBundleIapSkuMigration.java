package dev.getelements.elements.service.iap;

import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.IapSkuDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;

import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.model.iap.IapSkuReward;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

import static dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService.APPLE_IAP_SCHEME;
import static dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService.GOOGLE_IAP_SCHEME;
import static dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService.FACEBOOK_IAP_SCHEME;
import static dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService.OCULUS_IAP_SCHEME;

@ElementServiceExport(ProductBundleIapSkuMigration.class)
public class ProductBundleIapSkuMigration {

    private static final Logger logger = LoggerFactory.getLogger(ProductBundleIapSkuMigration.class);

    private ApplicationDao applicationDao;

    private ApplicationConfigurationDao applicationConfigurationDao;

    private Provider<Transaction> transactionProvider;

    @ElementEventConsumer(ElementLoader.SYSTEM_EVENT_ELEMENT_LOADED)
    public void init() {
        migrate(IosApplicationConfiguration.class, APPLE_IAP_SCHEME, IosApplicationConfiguration::getProductBundles);
        migrate(GooglePlayApplicationConfiguration.class, GOOGLE_IAP_SCHEME, GooglePlayApplicationConfiguration::getProductBundles);
        migrate(FacebookApplicationConfiguration.class, FACEBOOK_IAP_SCHEME, FacebookApplicationConfiguration::getProductBundles);
        migrate(OculusApplicationConfiguration.class, OCULUS_IAP_SCHEME, OculusApplicationConfiguration::getProductBundles);
    }

    private <T extends ApplicationConfiguration> void migrate(
            final Class<T> type,
            final String schema,
            final Function<T, List<ProductBundle>> getter) {

        int offset = 0;
        final int pageSize = 100;
        var page = getApplicationDao().getApplications(offset, pageSize);

        do {
            for (final var app : page.getObjects()) {

                final var configs = getApplicationConfigurationDao().getAllActiveApplicationConfigurations(app.getId(), type);

                for (final var config : configs) {

                    final var bundles = getter.apply(config);

                    if (bundles == null) {
                        continue;
                    }

                    for (final var bundle : bundles) {
                        migrateBundle(schema, bundle);
                    }
                }
            }

            offset += pageSize;

            if (page.getObjects().size() == pageSize) {
                page = getApplicationDao().getApplications(offset, pageSize);
            }

        } while (page.getObjects().size() == pageSize);
    }

    private void migrateBundle(final String schema, final ProductBundle bundle) {

        final var sku = new IapSku(
                null,
                schema,
                bundle.getProductId(),
                bundle.getProductBundleRewards().stream()
                        .map(r -> new IapSkuReward(r.getItemId(), r.getQuantity()))
                        .toList());

        try {
            getTransactionProvider().get().performAndCloseV(tx -> tx.getDao(IapSkuDao.class).createIapSku(sku));
            logger.info("Migrated ProductBundle -> IapSku: schema={} productId={}", schema, bundle.getProductId());
        } catch (DuplicateException e) {
            logger.debug("IapSku already exists for schema={} productId={}, skipping", schema, bundle.getProductId());
        }
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(final ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(final ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(final Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
