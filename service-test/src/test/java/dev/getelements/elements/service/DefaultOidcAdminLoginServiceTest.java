package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.service.auth.oidc.DefaultOidcAdminLoginService;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.inject.Guice.createInjector;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DefaultOidcAdminLoginServiceTest {

    @Inject
    private DefaultOidcAdminLoginService service;

    @Inject
    private OidcProviderConfigurationDao providerConfigurationDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    public void testReturnsOnlyAdminLoginEnabledProvidersAsMinimalProjection() {

        final var twitch = new OidcProviderConfiguration();
        twitch.setId("config-1");
        twitch.setName("twitch");
        twitch.setClientSecret("super-secret");
        twitch.setAdminLoginEnabled(true);
        twitch.setDisplayName("Twitch");
        twitch.setIconUrl("https://example.com/twitch-icon.png");

        when(providerConfigurationDao.getAdminLoginEnabledProviderConfigurations())
                .thenReturn(List.of(twitch));

        final var result = service.getAdminLoginProviders();

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId(), "config-1");
        assertEquals(result.get(0).getName(), "twitch");
        assertEquals(result.get(0).getDisplayName(), "Twitch");
        assertEquals(result.get(0).getIconUrl(), "https://example.com/twitch-icon.png");
    }

    @Test
    public void testNoAdminLoginEnabledProvidersReturnsEmptyList() {
        when(providerConfigurationDao.getAdminLoginEnabledProviderConfigurations())
                .thenReturn(List.of());

        assertTrue(service.getAdminLoginProviders().isEmpty());
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(OidcProviderConfigurationDao.class).toInstance(mock(OidcProviderConfigurationDao.class));
        }
    }

}
