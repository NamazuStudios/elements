package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;

import java.util.List;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Admin-facing management of the Elements OIDC provider's downstream client registrations. Secrets are
 * generated server-side when not supplied, are accepted on write only, and are never returned by any read.
 */
@ElementServiceExport
public interface OidcClientService {

    /**
     * Creates a client registration, generating a secret when one is not supplied.
     *
     * @param registration the registration to create
     * @return the created registration, with the raw {@code clientSecret} present exactly once (on this
     *         response) so the operator can hand it to the downstream application
     */
    OidcClientRegistration createClient(OidcClientRegistration registration);

    /**
     * Updates a client registration. A non-null {@code clientSecret} replaces the stored secret; a null one
     * retains it.
     *
     * @param registration the registration to update
     * @return the updated registration; {@code clientSecret} is echoed back only when it was rotated
     */
    OidcClientRegistration updateClient(OidcClientRegistration registration);

    /**
     * Deletes a client registration. Outstanding grants and refresh tokens for the client are revoked.
     *
     * @param clientId the client id of the registration to delete
     */
    void deleteClient(String clientId);

    /**
     * Fetches a client registration.
     *
     * @param clientId the client id
     * @return the registration
     */
    OidcClientRegistration getClient(String clientId);

    /**
     * Lists all client registrations.
     *
     * @return all registrations
     */
    List<OidcClientRegistration> getClients();

}
