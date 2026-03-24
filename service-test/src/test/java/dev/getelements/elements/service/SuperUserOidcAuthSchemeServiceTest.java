package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcAuthSchemeRequest;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.exception.ValidationFailureException;
import dev.getelements.elements.service.auth.oidc.SuperUserOidcAuthSchemeService;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.inject.Guice.createInjector;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class SuperUserOidcAuthSchemeServiceTest {

    @Inject
    private SuperUserOidcAuthSchemeService service;

    @Inject
    private OidcAuthSchemeDao authSchemeDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    public void testCreatePropagatesAllFields() {

        final var request = validRequest("Google", "https://accounts.google.com");
        request.setKeysUrl("https://www.googleapis.com/oauth2/v3/certs");
        request.setMediaType("application/jwk-set+json");

        final var saved = schemeWithId("scheme-1", "Google");
        when(authSchemeDao.createAuthScheme(any())).thenReturn(saved);

        final var response = service.createAuthScheme(request);

        final var captor = ArgumentCaptor.forClass(OidcAuthScheme.class);
        verify(authSchemeDao).createAuthScheme(captor.capture());

        final var entity = captor.getValue();
        assertEquals(entity.getName(), "Google");
        assertEquals(entity.getIssuer(), "https://accounts.google.com");
        assertEquals(entity.getKeysUrl(), "https://www.googleapis.com/oauth2/v3/certs");
        assertEquals(entity.getMediaType(), "application/jwk-set+json");
        assertNotNull(entity.getKeys());
        assertSame(response.getScheme(), saved);
    }

    @Test
    public void testUpdatePropagatesName() {

        final var existing = schemeWithId("scheme-1", "OldName");
        when(authSchemeDao.getAuthScheme("scheme-1")).thenReturn(existing);
        when(authSchemeDao.updateAuthScheme(any())).thenAnswer(i -> i.getArgument(0));

        service.updateAuthScheme("scheme-1", validRequest("NewName", "https://accounts.google.com"));

        final var captor = ArgumentCaptor.forClass(OidcAuthScheme.class);
        verify(authSchemeDao).updateAuthScheme(captor.capture());
        assertEquals(captor.getValue().getName(), "NewName");
    }

    @Test
    public void testUpdatePropagatesAllFields() {

        final var existing = schemeWithId("scheme-1", "Old");
        when(authSchemeDao.getAuthScheme("scheme-1")).thenReturn(existing);
        when(authSchemeDao.updateAuthScheme(any())).thenAnswer(i -> i.getArgument(0));

        final var request = validRequest("New", "https://new.issuer.com");
        request.setKeysUrl("https://new.issuer.com/keys");
        request.setMediaType("application/jwk-set+json");
        service.updateAuthScheme("scheme-1", request);

        final var captor = ArgumentCaptor.forClass(OidcAuthScheme.class);
        verify(authSchemeDao).updateAuthScheme(captor.capture());

        final var entity = captor.getValue();
        assertEquals(entity.getName(), "New");
        assertEquals(entity.getIssuer(), "https://new.issuer.com");
        assertEquals(entity.getKeysUrl(), "https://new.issuer.com/keys");
        assertEquals(entity.getMediaType(), "application/jwk-set+json");
    }

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testCreateNullNameFailsValidation() {
        service.createAuthScheme(validRequest(null, "https://accounts.google.com"));
    }

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testCreateNullIssuerFailsValidation() {
        service.createAuthScheme(validRequest("Google", null));
    }

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testCreateNullKeysFailsValidation() {
        final var request = new CreateOrUpdateOidcAuthSchemeRequest();
        request.setName("Google");
        request.setIssuer("https://accounts.google.com");
        // keys intentionally omitted
        service.createAuthScheme(request);
    }

    @Test
    public void testDeleteDelegatesToDao() {
        service.deleteAuthScheme("scheme-1");
        verify(authSchemeDao).deleteAuthScheme("scheme-1");
    }

    @Test
    public void testGetSchemeDelegatesToDao() {
        final var expected = schemeWithId("scheme-1", "Google");
        when(authSchemeDao.getAuthScheme("scheme-1")).thenReturn(expected);
        assertSame(service.getAuthScheme("scheme-1"), expected);
    }

    @Test
    public void testGetSchemesDelegatesToDao() {
        @SuppressWarnings("unchecked")
        final var page = (Pagination<OidcAuthScheme>) mock(Pagination.class);
        when(authSchemeDao.getAuthSchemes(5, 20, List.of("tag-a"))).thenReturn(page);
        assertSame(service.getAuthSchemes(5, 20, List.of("tag-a")), page);
    }

    // ---------- helpers ----------

    private static CreateOrUpdateOidcAuthSchemeRequest validRequest(final String name, final String issuer) {
        final var request = new CreateOrUpdateOidcAuthSchemeRequest();
        request.setName(name);
        request.setIssuer(issuer);
        request.setKeys(List.of(new JWK("RS256", "kid-1", "RSA", "sig", "AQAB", "sampleModulus")));
        return request;
    }

    private static OidcAuthScheme schemeWithId(final String id, final String name) {
        final var scheme = new OidcAuthScheme();
        scheme.setId(id);
        scheme.setName(name);
        scheme.setIssuer("https://accounts.google.com");
        scheme.setKeys(List.of());
        return scheme;
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(OidcAuthSchemeDao.class).toInstance(mock(OidcAuthSchemeDao.class));
            bind(jakarta.validation.Validator.class)
                    .toInstance(buildDefaultValidatorFactory().getValidator());
        }
    }

}
