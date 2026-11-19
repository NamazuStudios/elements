package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

/**
 * Verifies the one-Steam-configuration-per-application rule on the superuser create path. The
 * verification flow resolves the Steam configuration for an application and throws when more than one
 * is active, so a second config must be rejected at creation time with a clear error rather than
 * breaking every Steam receipt upload later.
 */
public class SuperUserSteamApplicationConfigurationServiceTest {

    private ApplicationConfigurationDao applicationConfigurationDao;

    private SuperUserSteamApplicationConfigurationService service;

    @BeforeMethod
    public void setUp() {
        applicationConfigurationDao = mock(ApplicationConfigurationDao.class);
        service = new SuperUserSteamApplicationConfigurationService();
        service.setApplicationConfigurationDao(applicationConfigurationDao);
    }

    @Test
    public void createsWhenNoSteamConfigurationExists() {

        when(applicationConfigurationDao.getAllActiveApplicationConfigurations(
                eq("app-1"), eq(SteamApplicationConfiguration.class))).thenReturn(List.of());

        service.createApplicationConfiguration("app-1", config("app-1", "steam-config", false));

        final var captor = ArgumentCaptor.forClass(SteamApplicationConfiguration.class);
        verify(applicationConfigurationDao).createApplicationConfiguration(eq("app-1"), captor.capture());
        assertEquals(captor.getValue().getSandbox(), Boolean.FALSE);

    }

    @Test
    public void rejectsASecondSteamConfigurationForTheSameApplication() {

        final var existing = config("app-1", "steam-config", false);
        when(applicationConfigurationDao.getAllActiveApplicationConfigurations(
                eq("app-1"), eq(SteamApplicationConfiguration.class))).thenReturn(List.of(existing));

        assertThrows(InvalidDataException.class,
                () -> service.createApplicationConfiguration("app-1", config("app-1", "steam-prod", true)));

        verify(applicationConfigurationDao, never())
                .createApplicationConfiguration(eq("app-1"), isA(SteamApplicationConfiguration.class));

    }

    private static SteamApplicationConfiguration config(final String parent, final String name, final boolean sandbox) {
        final var configuration = new SteamApplicationConfiguration();
        configuration.setName(name);
        configuration.setSandbox(sandbox);
        final var parentApplication = new dev.getelements.elements.sdk.model.application.Application();
        parentApplication.setId(parent);
        configuration.setParent(parentApplication);
        return configuration;
    }

}
