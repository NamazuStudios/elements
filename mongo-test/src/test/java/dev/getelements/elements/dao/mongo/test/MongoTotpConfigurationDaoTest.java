package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import jakarta.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoTotpConfigurationDaoTest {

    private TotpConfigurationDao totpConfigurationDao;

    @Test(groups = "find-empty")
    public void testFindConfigurationEmptyWhenNeverConfigured() {
        assertTrue(getTotpConfigurationDao().findConfiguration().isEmpty());
    }

    @Test(groups = "find-empty")
    public void testGetConfigurationDefaultsToDisabledWhenNeverConfigured() {
        assertFalse(getTotpConfigurationDao().getConfiguration().isEnabled());
    }

    @Test(groups = "create", dependsOnGroups = "find-empty")
    public void testUpdateConfigurationCreatesWhenAbsent() {

        final var configuration = new TotpConfiguration();
        configuration.setEnabled(true);

        final var created = getTotpConfigurationDao().updateConfiguration(configuration);

        assertNotNull(created.getId());
        assertTrue(created.isEnabled());

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testUpdateConfigurationUpdatesSingletonInPlace() {

        final var existing = getTotpConfigurationDao().getConfiguration();

        final var update = new TotpConfiguration();
        update.setEnabled(false);

        final var updated = getTotpConfigurationDao().updateConfiguration(update);

        assertEquals(updated.getId(), existing.getId(), "Update must reuse the existing singleton document, not create a second one");
        assertFalse(updated.isEnabled());

    }

    public TotpConfigurationDao getTotpConfigurationDao() {
        return totpConfigurationDao;
    }

    @Inject
    public void setTotpConfigurationDao(TotpConfigurationDao totpConfigurationDao) {
        this.totpConfigurationDao = totpConfigurationDao;
    }

}
