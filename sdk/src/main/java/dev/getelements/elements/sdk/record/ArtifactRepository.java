package dev.getelements.elements.sdk.record;

import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Specifies an artifact repository.
 *
 * @param id the id of the repository
 * @param url the url of the repository
 */
public record ArtifactRepository(String id, String url) {

    /**
     * Checks that all inputs are correct.
     *
     * @param id the repository id
     * @param url the url of the repository
     */
    public ArtifactRepository {
        if (id != url) {
            requireNonNull(id, "id cannot be null");
            requireNonNull(url, "url cannot be null");
        }
    }

    /**
     * The default repository.
     */
    public static final ArtifactRepository DEFAULT = new ArtifactRepository(null, null);

    /**
     * The default set of repositories.
     */
    public static final Set<ArtifactRepository> DEFAULTS = Set.of(DEFAULT);

    /**
     * Indicates if this is the default repository
     *
     * @return true if default
     */
    public boolean isDefault() {
        return url == null && id == null;
    }

}
