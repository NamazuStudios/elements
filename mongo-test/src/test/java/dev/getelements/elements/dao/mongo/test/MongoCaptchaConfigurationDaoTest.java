package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.CaptchaConfigurationDao;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaProvider;
import dev.getelements.elements.sdk.model.exception.auth.CaptchaConfigurationNotFoundException;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoCaptchaConfigurationDaoTest {

    private CaptchaConfigurationDao captchaConfigurationDao;

    @Test(groups = "find-empty")
    public void testFindConfigurationEmptyWhenNeverConfigured() {
        assertTrue(getCaptchaConfigurationDao().findConfiguration().isEmpty());
    }

    @Test(groups = "find-empty", expectedExceptions = CaptchaConfigurationNotFoundException.class)
    public void testGetConfigurationThrowsWhenNeverConfigured() {
        getCaptchaConfigurationDao().getConfiguration();
    }

    @Test(groups = "create", dependsOnGroups = "find-empty")
    public void testUpdateConfigurationCreatesWhenAbsent() {

        final var config = new CaptchaConfiguration();
        config.setProvider(CaptchaProvider.RECAPTCHA);
        config.setSiteKey("site-key");
        config.setSecretKey("super-secret");
        config.setEnabled(true);

        final var created = getCaptchaConfigurationDao().updateConfiguration(config);

        assertNotNull(created.getId());
        assertEquals(created.getProvider(), CaptchaProvider.RECAPTCHA);
        assertEquals(created.getSiteKey(), "site-key");
        assertEquals(created.getSecretKey(), "super-secret");
        assertTrue(created.isEnabled());

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testUpdateConfigurationUpdatesSingletonInPlace() {

        final var existing = getCaptchaConfigurationDao().getConfiguration();

        final var update = new CaptchaConfiguration();
        update.setProvider(CaptchaProvider.RECAPTCHA);
        update.setSiteKey("new-site-key");
        update.setSecretKey("new-secret");
        update.setEnabled(false);

        final var updated = getCaptchaConfigurationDao().updateConfiguration(update);

        assertEquals(updated.getId(), existing.getId(), "Update must reuse the existing singleton document, not create a second one");
        assertEquals(updated.getSiteKey(), "new-site-key");
        assertEquals(updated.getSecretKey(), "new-secret");
        assertFalse(updated.isEnabled());

    }

    public CaptchaConfigurationDao getCaptchaConfigurationDao() {
        return captchaConfigurationDao;
    }

    @Inject
    public void setCaptchaConfigurationDao(CaptchaConfigurationDao captchaConfigurationDao) {
        this.captchaConfigurationDao = captchaConfigurationDao;
    }

}
