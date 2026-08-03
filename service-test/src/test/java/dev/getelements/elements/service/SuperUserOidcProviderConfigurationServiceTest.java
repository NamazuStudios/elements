package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.auth.TokenEndpointAuthMethod;
import dev.getelements.elements.sdk.model.exception.ValidationFailureException;
import dev.getelements.elements.service.auth.oidc.OidcDiscoveryDocument;
import dev.getelements.elements.service.auth.oidc.OidcProviderConfigurationOperations;
import dev.getelements.elements.service.auth.oidc.SuperUserOidcProviderConfigurationService;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.inject.Guice.createInjector;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class SuperUserOidcProviderConfigurationServiceTest {

    @Inject
    private SuperUserOidcProviderConfigurationService service;

    @Inject
    private OidcProviderConfigurationDao providerConfigurationDao;

    @Inject
    private OidcProviderConfigurationOperations providerConfigurationOperations;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);

        final var discovery = new OidcDiscoveryDocument();
        discovery.setIssuer("https://id.twitch.tv/oauth2");
        discovery.setAuthorizationEndpoint("https://id.twitch.tv/oauth2/authorize");
        discovery.setTokenEndpoint("https://id.twitch.tv/oauth2/token");
        when(providerConfigurationOperations.resolveDiscovery(any())).thenReturn(discovery);
        when(providerConfigurationOperations.resolveScheme(any(), any())).thenReturn(new OidcAuthScheme());
    }

    @Test
    public void testCreatePropagatesFieldsResolvesDiscoveryAndRedactsSecret() {

        final var request = validRequest("twitch");

        // A real DAO would return a freshly-mapped object, not the same instance passed in — mirror that here so
        // the service's later config.setClientSecret(null) mutation (on the *returned* object) can't retroactively
        // affect what we captured as having been *sent* to the DAO.
        when(providerConfigurationDao.createProviderConfiguration(any())).thenAnswer(i -> {
            final var stored = copyOf(i.getArgument(0));
            stored.setId("config-1");
            return stored;
        });

        final var response = service.createProviderConfiguration(request);

        final var captor = ArgumentCaptor.forClass(OidcProviderConfiguration.class);
        verify(providerConfigurationDao).createProviderConfiguration(captor.capture());

        final var persisted = captor.getValue();
        assertEquals(persisted.getProvider(), "twitch");
        assertEquals(persisted.getClientId(), "client-id");
        assertEquals(persisted.getClientSecret(), "super-secret");
        assertEquals(persisted.getRedirectUri(), "https://api.example.com/oidc/twitch/callback");

        // The secret was passed to the DAO, but must never be echoed back to the caller.
        assertNull(response.getConfiguration().getClientSecret());
        assertEquals(response.getIssuer(), "https://id.twitch.tv/oauth2");
        assertEquals(response.getAuthorizationEndpoint(), "https://id.twitch.tv/oauth2/authorize");
        assertEquals(response.getTokenEndpoint(), "https://id.twitch.tv/oauth2/token");

        verify(providerConfigurationOperations).resolveDiscovery(any());
        verify(providerConfigurationOperations).resolveScheme(any(), any());

    }

    @Test
    public void testUpdatePropagatesFields() {

        when(providerConfigurationDao.updateProviderConfiguration(any())).thenAnswer(i -> copyOf(i.getArgument(0)));

        service.updateProviderConfiguration("config-1", validRequest("google"));

        final var captor = ArgumentCaptor.forClass(OidcProviderConfiguration.class);
        verify(providerConfigurationDao).updateProviderConfiguration(captor.capture());

        assertEquals(captor.getValue().getId(), "config-1");
        assertEquals(captor.getValue().getProvider(), "google");
        assertEquals(captor.getValue().getClientSecret(), "super-secret");

    }

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testCreateBlankProviderFailsValidation() {
        service.createProviderConfiguration(validRequest(""));
    }

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testCreateMissingClientSecretFailsValidation() {
        final var request = validRequest("twitch");
        request.setClientSecret(null);
        service.createProviderConfiguration(request);
    }

    @Test
    public void testUpdateWithBlankClientSecretPreservesExisting() {

        final var existing = new OidcProviderConfiguration();
        existing.setId("config-1");
        existing.setClientSecret("original-secret");
        when(providerConfigurationDao.getProviderConfiguration("config-1")).thenReturn(existing);
        when(providerConfigurationDao.updateProviderConfiguration(any())).thenAnswer(i -> copyOf(i.getArgument(0)));

        final var request = validRequest("twitch");
        request.setClientSecret(null);

        service.updateProviderConfiguration("config-1", request);

        final var captor = ArgumentCaptor.forClass(OidcProviderConfiguration.class);
        verify(providerConfigurationDao).updateProviderConfiguration(captor.capture());

        assertEquals(captor.getValue().getClientSecret(), "original-secret");

    }

    @Test
    public void testUpdateWithNewClientSecretOverwritesExisting() {

        final var existing = new OidcProviderConfiguration();
        existing.setId("config-1");
        existing.setClientSecret("original-secret");
        when(providerConfigurationDao.getProviderConfiguration("config-1")).thenReturn(existing);
        when(providerConfigurationDao.updateProviderConfiguration(any())).thenAnswer(i -> copyOf(i.getArgument(0)));

        service.updateProviderConfiguration("config-1", validRequest("twitch"));

        final var captor = ArgumentCaptor.forClass(OidcProviderConfiguration.class);
        verify(providerConfigurationDao).updateProviderConfiguration(captor.capture());

        // validRequest() sets clientSecret to "super-secret" — a non-blank value on update must overwrite,
        // not fall back to the existing stored secret.
        assertEquals(captor.getValue().getClientSecret(), "super-secret");
        verify(providerConfigurationDao, never()).getProviderConfiguration(any(String.class));

    }

    @Test
    public void testCreateWithBlankRedirectUriDefaultsToBuiltInCallback() {

        when(providerConfigurationDao.createProviderConfiguration(any())).thenAnswer(i -> copyOf(i.getArgument(0)));

        final var request = validRequest("twitch");
        request.setRedirectUri(null);

        service.createProviderConfiguration(request);

        final var captor = ArgumentCaptor.forClass(OidcProviderConfiguration.class);
        verify(providerConfigurationDao).createProviderConfiguration(captor.capture());

        assertEquals(captor.getValue().getRedirectUri(), "https://api.example.com/api/rest/oidc/twitch/callback");

    }

    @Test
    public void testUpdateWithBlankRedirectUriDefaultsToBuiltInCallback() {

        final var existing = new OidcProviderConfiguration();
        existing.setId("config-1");
        existing.setClientSecret("original-secret");
        when(providerConfigurationDao.getProviderConfiguration("config-1")).thenReturn(existing);
        when(providerConfigurationDao.updateProviderConfiguration(any())).thenAnswer(i -> copyOf(i.getArgument(0)));

        final var request = validRequest("google");
        request.setRedirectUri("");

        service.updateProviderConfiguration("config-1", request);

        final var captor = ArgumentCaptor.forClass(OidcProviderConfiguration.class);
        verify(providerConfigurationDao).updateProviderConfiguration(captor.capture());

        assertEquals(captor.getValue().getRedirectUri(), "https://api.example.com/api/rest/oidc/google/callback");

    }

    @Test
    public void testDeleteDelegatesToDao() {
        service.deleteProviderConfiguration("config-1");
        verify(providerConfigurationDao).deleteProviderConfiguration("config-1");
    }

    @Test
    public void testGetConfigurationDelegatesToDaoAndRedactsSecret() {
        final var expected = new OidcProviderConfiguration();
        expected.setId("config-1");
        expected.setClientSecret("super-secret");
        when(providerConfigurationDao.getProviderConfiguration("config-1")).thenReturn(expected);

        final var result = service.getProviderConfiguration("config-1");

        assertEquals(result.getId(), "config-1");
        assertNull(result.getClientSecret());
    }

    @Test
    public void testGetConfigurationsDelegatesToDaoAndRedactsSecret() {

        final var config = new OidcProviderConfiguration();
        config.setId("config-1");
        config.setClientSecret("super-secret");

        final var page = new Pagination<OidcProviderConfiguration>();
        page.setOffset(5);
        page.setTotal(1);
        page.setObjects(List.of(config));

        when(providerConfigurationDao.getProviderConfigurations(5, 20, List.of("tag-a"))).thenReturn(page);

        final var result = service.getProviderConfigurations(5, 20, List.of("tag-a"));

        assertEquals(result.getOffset(), 5);
        assertEquals(result.getTotal(), 1);
        assertEquals(result.getObjects().size(), 1);
        assertEquals(result.getObjects().get(0).getId(), "config-1");
        assertNull(result.getObjects().get(0).getClientSecret());
    }

    // ---------- helpers ----------

    // A real DAO returns a freshly-mapped object, not the same instance passed in. Mockito's captor holds a
    // reference, not a snapshot, so returning the same instance the service later mutates (e.g. redact()'s
    // setClientSecret(null)) would retroactively corrupt what the test captured as having been *sent* to the DAO.
    private static OidcProviderConfiguration copyOf(final OidcProviderConfiguration source) {
        final var copy = new OidcProviderConfiguration();
        copy.setId(source.getId());
        copy.setProvider(source.getProvider());
        copy.setDiscoveryUrl(source.getDiscoveryUrl());
        copy.setClientId(source.getClientId());
        copy.setClientSecret(source.getClientSecret());
        copy.setScopes(source.getScopes());
        copy.setRedirectUri(source.getRedirectUri());
        copy.setExtraAuthorizeParams(source.getExtraAuthorizeParams());
        copy.setTokenEndpointAuthMethod(source.getTokenEndpointAuthMethod());
        return copy;
    }

    private static CreateOrUpdateOidcProviderConfigurationRequest validRequest(final String provider) {
        final var request = new CreateOrUpdateOidcProviderConfigurationRequest();
        request.setProvider(provider);
        request.setDiscoveryUrl("https://id.twitch.tv/oauth2/.well-known/openid-configuration");
        request.setClientId("client-id");
        request.setClientSecret("super-secret");
        request.setRedirectUri("https://api.example.com/oidc/twitch/callback");
        request.setScopes(List.of("openid"));
        request.setTokenEndpointAuthMethod(TokenEndpointAuthMethod.CLIENT_SECRET_POST);
        return request;
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(OidcProviderConfigurationDao.class).toInstance(mock(OidcProviderConfigurationDao.class));
            // toProvider (not toInstance) — OidcProviderConfigurationOperations is a concrete class with its own
            // @Inject setters, and Guice member-injects toInstance() objects, which would otherwise require
            // bindings for OidcAuthSchemeDao/OidcDiscoveryCache's transitive dependencies too.
            bind(OidcProviderConfigurationOperations.class)
                    .toProvider(() -> mock(OidcProviderConfigurationOperations.class))
                    .in(com.google.inject.Singleton.class);
            bind(jakarta.validation.Validator.class)
                    .toInstance(buildDefaultValidatorFactory().getValidator());
            bind(String.class)
                    .annotatedWith(com.google.inject.name.Names.named(API_OUTSIDE_URL))
                    .toInstance("https://api.example.com/api/rest");
        }
    }

}
