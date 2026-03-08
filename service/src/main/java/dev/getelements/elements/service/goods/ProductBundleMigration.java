package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProductBundleDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
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

@ElementServiceExport(ProductBundleMigration.class)
public class ProductBundleMigration {

    private static final Logger logger = LoggerFactory.getLogger(ProductBundleMigration.class);

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
            final Function<T, List<dev.getelements.elements.sdk.model.application.ProductBundle>> getter) {

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
                        migrateBundle(app, schema, bundle);
                    }
                }
            }

            offset += pageSize;

            if (page.getObjects().size() == pageSize) {
                page = getApplicationDao().getApplications(offset, pageSize);
            }

        } while (page.getObjects().size() == pageSize);
    }

    private void migrateBundle(
            final dev.getelements.elements.sdk.model.application.Application app,
            final String schema,
            final dev.getelements.elements.sdk.model.application.ProductBundle bundle) {

        final var standalone = new dev.getelements.elements.sdk.model.goods.ProductBundle();
        standalone.setSchema(schema);
        standalone.setApplication(app);
        standalone.setProductId(bundle.getProductId());
        standalone.setDisplayName(bundle.getDisplayName());
        standalone.setDescription(bundle.getDescription());
        standalone.setProductBundleRewards(bundle.getProductBundleRewards());
        standalone.setMetadata(bundle.getMetadata());
        standalone.setDisplay(bundle.getDisplay());

        try {
            getTransactionProvider().get().performAndCloseV(
                    tx -> tx.getDao(ProductBundleDao.class).createProductBundle(standalone));
            logger.info("Migrated application/ProductBundle -> goods/ProductBundle: app={} schema={} productId={}",
                    app.getId(), schema, bundle.getProductId());
        } catch (DuplicateException e) {
            logger.debug("ProductBundle already exists for app={} schema={} productId={}, skipping",
                    app.getId(), schema, bundle.getProductId());
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
