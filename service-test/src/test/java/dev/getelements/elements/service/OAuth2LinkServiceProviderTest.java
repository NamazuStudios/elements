package dev.getelements.elements.service;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.User.Level;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthService;
import dev.getelements.elements.service.auth.oauth2.OAuth2LinkServiceProvider;
import dev.getelements.elements.service.auth.oauth2.UserOAuth2AuthService;
import jakarta.inject.Provider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class OAuth2LinkServiceProviderTest {

    private static OAuth2LinkServiceProvider providerFor(Level level) {
        final var user = new User();
        user.setLevel(level);

        @SuppressWarnings("unchecked")
        final Provider<UserOAuth2AuthService> userProvider = mock(Provider.class);
        when(userProvider.get()).thenReturn(mock(UserOAuth2AuthService.class));

        final var p = new OAuth2LinkServiceProvider();
        p.setUser(user);
        p.setUserOAuth2AuthServiceProvider(userProvider);
        return p;
    }

    @Test
    public void testUserLevel_returnsUserOAuth2AuthService() {
        final var service = providerFor(Level.USER).get();
        assertNotNull(service);
        assertTrue(service instanceof UserOAuth2AuthService);
    }

    @Test
    public void testSuperuserLevel_returnsUserOAuth2AuthService() {
        final var service = providerFor(Level.SUPERUSER).get();
        assertNotNull(service);
        assertTrue(service instanceof UserOAuth2AuthService);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testUnprivilegedLevel_throwsForbiddenException() {
        providerFor(Level.UNPRIVILEGED).get();
    }
}
