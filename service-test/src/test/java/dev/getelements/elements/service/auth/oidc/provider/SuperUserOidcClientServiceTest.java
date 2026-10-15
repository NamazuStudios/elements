package dev.getelements.elements.service.auth.oidc.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.auth.OidcProviderGrant;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.security.PasswordGenerator;
import dev.getelements.elements.sdk.service.auth.OidcClientService;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the superuser-scoped client registration service: id/secret generation, write-only secret
 * echo, validation, and cascading revocation when a registration is deleted.
 */
public class SuperUserOidcClientServiceTest {

    private static final String CLIENT_ID = "el_generated";

    private OidcProviderDao oidcProviderDao;

    private SessionDao sessionDao;

    private PasswordGenerator passwordGenerator;

    @Inject
    private SuperUserOidcClientService service;

    @BeforeMethod
    public void setup() {

        oidcProviderDao = mock(OidcProviderDao.class);
        sessionDao = mock(SessionDao.class);
        passwordGenerator = mock(PasswordGenerator.class);

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(OidcProviderDao.class).toInstance(oidcProviderDao);
                bind(SessionDao.class).toInstance(sessionDao);
                bind(PasswordGenerator.class).toInstance(passwordGenerator);
                bind(SuperUserOidcClientService.class);
            }
        }).injectMembers(this);

        when(passwordGenerator.generate()).thenReturn("generated-secret");
        when(oidcProviderDao.createClient(any())).then(i -> i.getArgument(0));
        when(oidcProviderDao.updateClient(any())).then(i -> i.getArgument(0));

    }

    private OidcClientRegistration registration() {
        final var registration = new OidcClientRegistration();
        registration.setDisplayName("Test client");
        registration.setRedirectUris(Set.of("https://client.example.com/callback"));
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.AUTHORIZATION_CODE));
        registration.setScopes(Set.of("openid"));
        return registration;
    }

    private OidcClientRegistration machineRegistration() {
        final var registration = new OidcClientRegistration();
        registration.setDisplayName("Machine client");
        registration.setGrantTypes(Set.of(OidcClientRegistration.GrantType.CLIENT_CREDENTIALS));
        registration.setAudiences(Set.of("filebrowser"));
        registration.setScopes(Set.of("openid"));
        return registration;
    }

    @Test
    public void testCreateGeneratesIdAndSecretAndEchoesOnce() {

        final var created = service.createClient(registration());

        assertNotNull(created.getClientId());
        assertTrue(created.getClientId().startsWith("el_"), "Generated ids are namespaced: " + created.getClientId());
        assertEquals(created.getClientSecret(), "generated-secret");
        assertEquals(created.getStatus(), OidcClientRegistration.Status.ACTIVE);

        // The raw secret is persisted only via the DAO (which hashes it); the model never round-trips it.
        final var persisted = org.mockito.ArgumentCaptor.forClass(OidcClientRegistration.class);
        verify(oidcProviderDao).createClient(persisted.capture());
        assertEquals(persisted.getValue().getClientId(), created.getClientId());
        assertEquals(persisted.getValue().getClientSecret(), "generated-secret");

    }

    @Test
    public void testCreateHonorsSuppliedIdAndSecret() {

        final var registration = registration();
        registration.setClientId("el_explicit");
        registration.setClientSecret("explicit-secret");

        final var created = service.createClient(registration);

        assertEquals(created.getClientId(), "el_explicit");
        assertEquals(created.getClientSecret(), "explicit-secret");
        verify(passwordGenerator, never()).generate();

    }

    @Test
    public void testCreateRejectsMissingScopes() {
        final var registration = registration();
        registration.setScopes(null);
        assertThrows(InvalidDataException.class, () -> service.createClient(registration));
    }

    @Test
    public void testCreateRejectsSpacesInScopes() {
        final var registration = registration();
        registration.setScopes(Set.of("openid profile"));
        assertThrows(InvalidDataException.class, () -> service.createClient(registration));
    }

    @Test
    public void testCreateRejectsAuthorizationCodeClientWithoutRedirectUris() {
        final var registration = registration();
        registration.setRedirectUris(null);
        assertThrows(InvalidDataException.class, () -> service.createClient(registration));
    }

    @Test
    public void testCreateAllowsMachineClientWithoutRedirectUris() {

        final var created = service.createClient(machineRegistration());

        assertNotNull(created.getClientId());
        assertNull(created.getRedirectUris());

    }

    @Test
    public void testUpdateRotatesSecretOnlyWhenSupplied() {

        when(oidcProviderDao.getClient("el_existing")).thenReturn(Optional.of(registration()));

        // Retained: no clientSecret in the update payload.
        final var retained = service.updateClient(registration());
        assertEquals(retained.getClientSecret(), null);

        // Rotated: the new secret is echoed back exactly once.
        final var rotated = registration();
        rotated.setClientSecret("new-secret");
        final var echoed = service.updateClient(rotated);
        assertEquals(echoed.getClientSecret(), "new-secret");

    }

    @Test
    public void testDeleteRevokesGrantsAndBackingSessions() {

        final var existing = registration();
        existing.setClientId("el_generated");
        when(oidcProviderDao.getClients()).thenReturn(List.of(existing));

        final var grant = new OidcProviderGrant();
        grant.setId("grant-1");
        grant.setClientId("el_generated");
        grant.setUserId("user-1");
        grant.setSessionId("session-1");

        when(oidcProviderDao.getGrantsForClient("el_generated")).thenReturn(List.of(grant));

        service.deleteClient("el_generated");

        verify(oidcProviderDao).revokeRefreshTokensForGrant("grant-1");
        verify(oidcProviderDao).deleteGrantBySessionId("session-1");
        verify(sessionDao).deleteSessionBySessionId("session-1");
        verify(oidcProviderDao).deleteClient("el_generated");

    }

    @Test
    public void testDeleteUnknownClientFails() {
        when(oidcProviderDao.getClients()).thenReturn(List.of());
        assertThrows(NotFoundException.class, () -> service.deleteClient("el_unknown"));
    }

    @Test
    public void testGetClientFailsWhenMissing() {
        when(oidcProviderDao.getClient("el_missing")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getClient("el_missing"));
    }

    public SuperUserOidcClientService getService() {
        return service;
    }

}
