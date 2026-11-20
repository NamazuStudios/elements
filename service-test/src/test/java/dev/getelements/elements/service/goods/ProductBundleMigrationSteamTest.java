package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProductBundleDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Deterministic coverage for the product-bundle migration's Steam entry: bundles embedded in a Steam
 * application configuration must migrate into the standalone product-bundle catalog (the Commerce tab's
 * data source), exactly like the Apple, Google Play, Facebook and Oculus configurations. The Steam type
 * was missing from the migration list, so Steam config bundles never reached the Commerce tab.
 */
public class ProductBundleMigrationSteamTest {

    private ApplicationDao applicationDao;

    private ApplicationConfigurationDao applicationConfigurationDao;

    private ProductBundleDao productBundleDao;

    private Transaction transaction;

    private ProductBundleMigration migration;

    @BeforeMethod
    public void setUp() {

        applicationDao = mock(ApplicationDao.class);
        applicationConfigurationDao = mock(ApplicationConfigurationDao.class);
        productBundleDao = mock(ProductBundleDao.class);
        transaction = mock(Transaction.class);

        when(transaction.getDao(ProductBundleDao.class)).thenReturn(productBundleDao);

        // A mocked Transaction's performAndCloseV swallows the lambda by default; execute it against
        // this mock so the migration's per-bundle createProductBundle calls actually run.
        org.mockito.Mockito.doAnswer(invocation -> {
            final var op = invocation.getArgument(0, java.util.function.Consumer.class);
            op.accept(transaction);
            return null;
        }).when(transaction).performAndCloseV(any());

        final var transactionProvider = mock(jakarta.inject.Provider.class);
        when(transactionProvider.get()).thenReturn(transaction);

        migration = new ProductBundleMigration();
        migration.setApplicationDao(applicationDao);
        migration.setApplicationConfigurationDao(applicationConfigurationDao);
        migration.setTransactionProvider(transactionProvider);
    }

    @Test
    public void steamApplicationConfigurationBundlesMigrateIntoTheCatalog() throws Exception {

        final var application = mock(Application.class);
        when(application.getId()).thenReturn("app-steam");

        final var configuration = new SteamApplicationConfiguration();
        configuration.setProductBundles(steamBundles());

        when(applicationConfigurationDao.getAllActiveApplicationConfigurations(
                eq("app-steam"), eq(SteamApplicationConfiguration.class)))
                .thenReturn(List.of(configuration));

        when(applicationDao.getApplications(anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    final var offset = invocation.getArgument(0, Integer.class);
                    return offset == 0 ? pagination(application) : pagination();
                });

        migration.init();

        final var captor = ArgumentCaptor.forClass(dev.getelements.elements.sdk.model.goods.ProductBundle.class);
        verify(productBundleDao, times(2)).createProductBundle(captor.capture());

        final var migrated = captor.getAllValues();
        assertEquals(migrated.size(), 2, "both Steam config bundles must migrate into the catalog");
        assertEquals(migrated.get(0).getProductId(), "com.example.steam.gold.1000");
        assertEquals(migrated.get(0).getSchema(), "com.valvesoftware.steam");
        assertEquals(migrated.get(1).getProductId(), "com.example.steam.starter.pack");

    }

    private Pagination<Application> pagination(final Application... applications) {

        final var page = new Pagination<Application>();
        page.setOffset(0);
        page.setTotal(applications.length);
        page.setObjects(List.of(applications));

        return page;

    }

    private List<dev.getelements.elements.sdk.model.application.ProductBundle> steamBundles() {

        final var first = new dev.getelements.elements.sdk.model.application.ProductBundle();
        first.setProductId("com.example.steam.gold.1000");
        first.setDisplayName("1000 Gold");
        first.setDisplay(true);

        final var second = new dev.getelements.elements.sdk.model.application.ProductBundle();
        second.setProductId("com.example.steam.starter.pack");
        second.setDisplayName("Starter Pack");
        second.setDisplay(true);

        return List.of(first, second);

    }

}
