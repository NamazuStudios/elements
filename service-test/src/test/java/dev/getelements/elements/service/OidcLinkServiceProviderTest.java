package dev.getelements.elements.service;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.User.Level;
import dev.getelements.elements.service.auth.oidc.OidcLinkServiceProvider;
import dev.getelements.elements.service.auth.oidc.UserOidcAuthService;
import jakarta.inject.Provider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class OidcLinkServiceProviderTest {

    private static OidcLinkServiceProvider providerFor(Level level) {
        final var user = new User();
        user.setLevel(level);

        @SuppressWarnings("unchecked")
        final Provider<UserOidcAuthService> userProvider = mock(Provider.class);
        when(userProvider.get()).thenReturn(mock(UserOidcAuthService.class));

        final var p = new OidcLinkServiceProvider();
        p.setUser(user);
        p.setUserOidcAuthServiceProvider(userProvider);
        return p;
    }

    @Test
    public void testUserLevel_returnsUserOidcAuthService() {
        final var service = providerFor(Level.USER).get();
        assertNotNull(service);
        assertTrue(service instanceof UserOidcAuthService);
    }

    @Test
    public void testSuperuserLevel_returnsUserOidcAuthService() {
        final var service = providerFor(Level.SUPERUSER).get();
        assertNotNull(service);
        assertTrue(service instanceof UserOidcAuthService);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testUnprivilegedLevel_throwsForbiddenException() {
        providerFor(Level.UNPRIVILEGED).get();
    }
}
