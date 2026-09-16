package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.OidcAdminLoginProvider;

import java.util.List;

/**
 * Publicly-accessible (no authentication required) service listing the OIDC providers offered as login
 * options on the admin panel's login page. Identical behavior regardless of caller identity, since the
 * caller has no session yet when this is consulted.
 */
@ElementPublic
@ElementServiceExport
public interface OidcAdminLoginService {

    /**
     * Lists the OIDC providers that have opted into admin-panel login.
     *
     * @return the list of {@link OidcAdminLoginProvider} options
     */
    List<OidcAdminLoginProvider> getAdminLoginProviders();

}
