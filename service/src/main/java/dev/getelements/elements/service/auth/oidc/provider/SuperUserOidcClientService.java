package dev.getelements.elements.service.auth.oidc.provider;

import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.security.PasswordGenerator;
import dev.getelements.elements.sdk.service.auth.OidcClientService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SuperUserOidcClientService implements OidcClientService {

    private static final Logger logger = LoggerFactory.getLogger(SuperUserOidcClientService.class);

    private static final int CLIENT_ID_BYTES = 16;

    private OidcProviderDao oidcProviderDao;

    private SessionDao sessionDao;

    private PasswordGenerator passwordGenerator;

    @Override
    public OidcClientRegistration createClient(final OidcClientRegistration registration) {

        validate(registration);

        if (registration.getClientId() == null || registration.getClientId().isBlank()) {
            registration.setClientId(generateClientId());
        }

        if (registration.getClientSecret() == null || registration.getClientSecret().isBlank()) {
            registration.setClientSecret(getPasswordGenerator().generate());
        }

        registration.setStatus(OidcClientRegistration.Status.ACTIVE);

        final var created = getOidcProviderDao().createClient(registration);

        // Echo the raw secret back exactly once so the operator can hand it to the downstream application.

        final var withSecret = toEchoed(created, registration.getClientSecret());
        logger.info("Created OIDC client registration {} ({}).", created.getClientId(), created.getDisplayName());

        return withSecret;

    }

    @Override
    public OidcClientRegistration updateClient(final OidcClientRegistration registration) {

        validate(registration);

        final var updated = getOidcProviderDao().updateClient(registration);

        // A blank secret retains the stored one, mirroring create's generate-when-blank posture: an admin
        // saving unrelated edits must not silently rotate the credential to the empty string.
        if (registration.getClientSecret() != null && !registration.getClientSecret().isBlank()) {
            logger.info("Rotated secret for OIDC client registration {}.", updated.getClientId());
            return toEchoed(updated, registration.getClientSecret());
        }

        return updated;

    }

    @Override
    public void deleteClient(final String clientId) {

        // Revoking registrations must not leave orphaned live grants: every grant the client holds dies with
        // it, along with its backing sessions and refresh tokens — tokens already issued downstream stop
        // verifying immediately, exactly as end-session does.

        getOidcProviderDao().getClients().stream()
                .filter(r -> r.getClientId().equals(clientId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No client registration " + clientId));

        for (final var grant : getOidcProviderDao().getGrantsForClient(clientId)) {
            getOidcProviderDao().revokeRefreshTokensForGrant(grant.getId());
            getOidcProviderDao().deleteGrantBySessionId(grant.getSessionId());
            getSessionDao().deleteSessionBySessionId(grant.getSessionId());
            logger.info("Revoked grant {} on deleted client {}.", grant.getId(), clientId);
        }

        getOidcProviderDao().deleteClient(clientId);

        logger.info("Deleted OIDC client registration {}.", clientId);

    }

    @Override
    public OidcClientRegistration getClient(final String clientId) {
        return getOidcProviderDao().getClient(clientId)
                .orElseThrow(() -> new NotFoundException("No client registration " + clientId));
    }

    @Override
    public List<OidcClientRegistration> getClients() {
        return getOidcProviderDao().getClients();
    }

    private void validate(final OidcClientRegistration registration) {

        final var machineOnly = registration.getGrantTypes() != null && !registration.getGrantTypes().isEmpty()
                && registration.getGrantTypes().stream()
                        .allMatch(gt -> gt == OidcClientRegistration.GrantType.CLIENT_CREDENTIALS);

        // Redirect URIs gate the browser redirect: any client that can start the authorization-code flow
        // must have at least one. Machine-only clients never redirect and may omit them.

        if (!machineOnly && (registration.getRedirectUris() == null || registration.getRedirectUris().isEmpty())) {
            throw new InvalidDataException(
                    "At least one redirect URI is required for clients using the authorization code flow.");
        }

        if (registration.getGrantTypes() == null || registration.getGrantTypes().isEmpty()) {
            throw new InvalidDataException("At least one grant type is required.");
        }

        if (registration.getScopes() == null || registration.getScopes().isEmpty()) {
            throw new InvalidDataException("At least one scope is required.");
        }

        for (final var scope : registration.getScopes()) {
            if (scope.contains(" ")) {
                throw new InvalidDataException("Scopes must not contain spaces: " + scope);
            }
        }

    }

    private OidcClientRegistration toEchoed(final OidcClientRegistration stored, final String rawSecret) {
        final var echoed = new OidcClientRegistration();
        copy(stored, echoed);
        echoed.setClientSecret(rawSecret);
        return echoed;
    }

    private void copy(final OidcClientRegistration source, final OidcClientRegistration target) {
        target.setClientId(source.getClientId());
        target.setDisplayName(source.getDisplayName());
        target.setRedirectUris(source.getRedirectUris());
        target.setGrantTypes(source.getGrantTypes());
        target.setScopes(source.getScopes());
        target.setAudiences(source.getAudiences());
        target.setStatus(source.getStatus());
        target.setApplicationId(source.getApplicationId());
        target.setCreatedAt(source.getCreatedAt());
    }

    private String generateClientId() {
        final var bytes = new byte[CLIENT_ID_BYTES];
        new java.security.SecureRandom().nextBytes(bytes);
        return "el_" + java.util.HexFormat.of().formatHex(bytes);
    }

    public OidcProviderDao getOidcProviderDao() {
        return oidcProviderDao;
    }

    @Inject
    public void setOidcProviderDao(OidcProviderDao oidcProviderDao) {
        this.oidcProviderDao = oidcProviderDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    @Inject
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

}
