package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.auth.JwtSigningKey;

import java.util.List;
import java.util.Optional;

/**
 * Persists the server's own session-token signing keys. Keys are stored with their private halves
 * encrypted at rest; this DAO is responsible only for storage and retrieval. Key generation and
 * encryption belong to the token issuer.
 *
 * Multiple keys may be active at once so that rotation does not invalidate outstanding tokens: the
 * issuer signs with the most recently created active key, while verification accepts tokens signed
 * by any active or retired key which has not passed its retention window.
 *
 * Note that this DAO is intentionally not exported to Elements: signing keys are internal server
 * infrastructure, and their private halves must never be visible outside the server.
 */
public interface JwtSigningKeyDao {

    /**
     * Returns all signing keys regardless of status, newest first.
     *
     * @return all signing keys
     */
    List<JwtSigningKey> getSigningKeys();

    /**
     * Returns all signing keys in the supplied status, newest first.
     *
     * @param status the status to filter on
     * @return the matching signing keys
     */
    List<JwtSigningKey> getSigningKeys(JwtSigningKey.Status status);

    /**
     * Fetches a specific signing key by its key id.
     *
     * @param kid the key id
     * @return an {@link Optional<JwtSigningKey>}, never null
     */
    Optional<JwtSigningKey> findSigningKey(String kid);

    /**
     * Persists a new signing key.
     *
     * @param jwtSigningKey the signing key to save
     * @return the saved signing key
     */
    JwtSigningKey createSigningKey(JwtSigningKey jwtSigningKey);

    /**
     * Updates the supplied signing key, typically to change its status during rotation.
     *
     * @param jwtSigningKey the signing key to update
     * @return the updated signing key
     */
    JwtSigningKey updateSigningKey(JwtSigningKey jwtSigningKey);

}
