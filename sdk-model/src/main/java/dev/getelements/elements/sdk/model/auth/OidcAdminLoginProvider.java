package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * A minimal, publicly-visible projection of an {@link OidcProviderConfiguration} that has opted into
 * admin-panel login, for rendering login options on the admin panel's (unauthenticated) login page.
 * Deliberately excludes everything else on the configuration (client id/secret, URLs, etc.).
 */
public class OidcAdminLoginProvider implements Serializable {

    /** Creates a new instance. */
    public OidcAdminLoginProvider() {}

    @Schema(description = "The unique ID of the provider configuration.")
    private String id;

    @Schema(description = "The provider's unique identifier (e.g. 'twitch'), passed as the 'provider' value " +
            "to the OIDC session endpoints to start a login attempt.")
    private String name;

    @Schema(description = "Human-readable name to display on the login button. Falls back to name if unset.")
    private String displayName;

    @Schema(description = "Optional icon/logo URL to display on the login button.")
    private String iconUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OidcAdminLoginProvider that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(iconUrl, that.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayName, iconUrl);
    }

    @Override
    public String toString() {
        return "OidcAdminLoginProvider{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }

}
