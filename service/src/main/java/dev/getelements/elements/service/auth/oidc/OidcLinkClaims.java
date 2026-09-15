package dev.getelements.elements.service.auth.oidc;

import java.util.Map;

/**
 * External-identity claims validated by the callback for a linking login attempt, persisted on
 * {@code OidcLoginAttempt#getLinkClaimsJson()} while the mutation itself is deferred to the confirm flow. Internal
 * wire-format DTO only.
 */
public class OidcLinkClaims {

    private String schemeName;

    private String externalUserId;

    private String email;

    private Map<String, String> profileClaims;

    private String applicationNameOrId;

    private boolean applicationExplicitlyRequested;

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, String> getProfileClaims() {
        return profileClaims;
    }

    public void setProfileClaims(Map<String, String> profileClaims) {
        this.profileClaims = profileClaims;
    }

    public String getApplicationNameOrId() {
        return applicationNameOrId;
    }

    public void setApplicationNameOrId(String applicationNameOrId) {
        this.applicationNameOrId = applicationNameOrId;
    }

    public boolean isApplicationExplicitlyRequested() {
        return applicationExplicitlyRequested;
    }

    public void setApplicationExplicitlyRequested(boolean applicationExplicitlyRequested) {
        this.applicationExplicitlyRequested = applicationExplicitlyRequested;
    }

}
