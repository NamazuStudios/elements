package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.dao.mongo.model.auth.MongoOidcAuthorizationCode;
import dev.getelements.elements.dao.mongo.model.auth.MongoOidcClient;
import dev.getelements.elements.dao.mongo.model.auth.MongoOidcProviderGrant;
import dev.getelements.elements.dao.mongo.model.auth.MongoOidcRefreshToken;
import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.auth.OidcProviderGrant;
import dev.getelements.elements.sdk.model.auth.OidcRefreshToken;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.bson.types.ObjectId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static com.mongodb.client.model.ReturnDocument.BEFORE;
import static dev.getelements.elements.sdk.model.Constants.PASSWORD_DIGEST;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.gte;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * MongoDB implementation of {@link OidcProviderDao}. Authorization-code redemption and refresh-token
 * consumption are atomic, status-guarded transitions (mirroring {@code MongoOidcLoginAttemptDao}); the
 * expiring-collection reads defensively re-check expiry because Mongo's TTL sweep is asynchronous.
 */
@Singleton
public class MongoOidcProviderDao implements OidcProviderDao {

    private static final int SALT_LENGTH = 16;

    private Datastore datastore;

    private Provider<MessageDigest> passwordDigestProvider;

    //
    // Client registrations
    //

    @Override
    public OidcClientRegistration createClient(final OidcClientRegistration registration) {

        if (getClient(registration.getClientId()).isPresent()) {
            throw new dev.getelements.elements.sdk.model.exception.ConflictException(
                    "A client registration already exists with clientId " + registration.getClientId());
        }

        final var entity = toEntity(registration);
        hashSecret(entity, registration.getClientSecret());
        getDatastore().insert(entity);

        return toModel(entity);

    }

    @Override
    public OidcClientRegistration updateClient(final OidcClientRegistration registration) {

        final var existing = getDatastore().find(MongoOidcClient.class)
                .filter(eq("_id", registration.getClientId()))
                .first();

        if (existing == null) {
            throw new NotFoundException("No client registration " + registration.getClientId());
        }

        final var entity = toEntity(registration);
        if (registration.getClientSecret() != null) {
            hashSecret(entity, registration.getClientSecret());
        } else {
            entity.setSecretSalt(existing.getSecretSalt());
            entity.setSecretHash(existing.getSecretHash());
            entity.setHashAlgorithm(existing.getHashAlgorithm());
        }

        getDatastore().save(entity);

        return toModel(entity);

    }

    @Override
    public void deleteClient(final String clientId) {
        getDatastore().find(MongoOidcClient.class).filter(eq("_id", clientId)).delete();
    }

    @Override
    public Optional<OidcClientRegistration> getClient(final String clientId) {
        final var entity = getDatastore().find(MongoOidcClient.class)
                .filter(eq("_id", clientId))
                .first();
        return Optional.ofNullable(entity).map(this::toModel);
    }

    @Override
    public List<OidcClientRegistration> getClients() {
        return getDatastore().find(MongoOidcClient.class)
                .iterator(new FindOptions().sort(Sort.ascending("displayName")))
                .toList()
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public boolean verifyClientSecret(final String clientId, final String clientSecret) {
        final var entity = getDatastore().find(MongoOidcClient.class)
                .filter(eq("_id", clientId))
                .first();
        return entity != null && verifyClientSecret(entity, clientSecret);
    }

    //
    // Authorization codes
    //

    @Override
    public OidcAuthorizationCode createAuthorizationCode(final OidcAuthorizationCode code) {
        final var entity = toEntity(code);
        getDatastore().insert(entity);
        return toModel((MongoOidcAuthorizationCode) entity);
    }

    @Override
    public Optional<OidcAuthorizationCode> redeemAuthorizationCode(final String code) {

        // returnDocument(AFTER), mirroring MongoOidcLoginAttemptDao: a concurrent second redemption no longer
        // matches the status guard and returns null. The expiry filter makes an expired-but-not-yet-swept code
        // fail atomically rather than relying on the async TTL sweep.
        final var query = getDatastore().find(MongoOidcAuthorizationCode.class)
                .filter(and(
                        eq("_id", code),
                        eq("status", OidcAuthorizationCode.Status.PENDING),
                        gte("expiry", new Timestamp(System.currentTimeMillis()))
                ));

        final var entity = query.modify(
                set("status", OidcAuthorizationCode.Status.REDEEMED)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        return Optional.ofNullable(entity).map(this::toModel);

    }

    @Override
    public Optional<OidcAuthorizationCode> completeAuthorizationCode(final String code,
                                                                     final String userId,
                                                                     final long authTime) {

        // userId == null matches documents where the field is absent (awaiting login). A concurrent second
        // completion attempt no longer matches and returns null. The code enters AWAITING_CONSENT rather
        // than completing outright: when the deployment does not require consent, the authorization service
        // immediately records the implicit approval through decideConsent, restoring PENDING.

        final var query = getDatastore().find(MongoOidcAuthorizationCode.class)
                .filter(and(
                        eq("_id", code),
                        eq("status", OidcAuthorizationCode.Status.PENDING),
                        eq("userId", null),
                        gte("expiry", new Timestamp(System.currentTimeMillis()))
                ));

        final var entity = query.modify(
                set("userId", userId),
                set("authTime", authTime),
                set("status", OidcAuthorizationCode.Status.AWAITING_CONSENT)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        return Optional.ofNullable(entity).map(this::toModel);

    }

    @Override
    public Optional<OidcAuthorizationCode> decideConsent(final String code, final boolean approved) {

        // returnDocument(AFTER), mirroring the code's other transitions: a concurrent second decision no
        // longer matches the status guard and returns null. The expiry filter makes an
        // expired-but-not-yet-swept code fail atomically rather than relying on the async TTL sweep.

        final var query = getDatastore().find(MongoOidcAuthorizationCode.class)
                .filter(and(
                        eq("_id", code),
                        eq("status", OidcAuthorizationCode.Status.AWAITING_CONSENT),
                        gte("expiry", new Timestamp(System.currentTimeMillis()))
                ));

        final var entity = query.modify(
                set("status", approved
                        ? OidcAuthorizationCode.Status.PENDING
                        : OidcAuthorizationCode.Status.DENIED)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        return Optional.ofNullable(entity).map(this::toModel);

    }

    //
    // Grants
    //

    @Override
    public OidcProviderGrant createGrant(final OidcProviderGrant grant) {

        final var entity = toEntity(grant);

        if (entity.getId() == null) {
            entity.setId(new ObjectId().toString());
        }

        getDatastore().insert(entity);

        return toModel((MongoOidcProviderGrant) entity);

    }

    @Override
    public Optional<OidcProviderGrant> getGrant(final String grantId) {

        final var entity = getDatastore().find(MongoOidcProviderGrant.class)
                .filter(and(
                        eq("_id", grantId),
                        gte("expiry", new Timestamp(System.currentTimeMillis()))
                ))
                .first();

        return Optional.ofNullable(entity).map(this::toModel);

    }

    @Override
    public List<OidcProviderGrant> getGrantsForUserAndClient(final String userId, final String clientId) {

        return getDatastore().find(MongoOidcProviderGrant.class)
                .filter(and(
                        eq("userId", userId),
                        eq("clientId", clientId),
                        gte("expiry", new Timestamp(System.currentTimeMillis()))
                ))
                .iterator()
                .toList()
                .stream()
                .map(this::toModel)
                .toList();

    }

    @Override
    public Optional<OidcProviderGrant> findGrantBySessionId(final String sessionId) {

        final var entity = getDatastore().find(MongoOidcProviderGrant.class)
                .filter(and(
                        eq("sessionId", sessionId),
                        gte("expiry", new Timestamp(System.currentTimeMillis()))
                ))
                .first();

        return Optional.ofNullable(entity).map(this::toModel);

    }

    @Override
    public Optional<OidcProviderGrant> rotateGrantSession(final String grantId,
                                                          final String oldSessionId,
                                                          final String newSessionId) {

        final var query = getDatastore().find(MongoOidcProviderGrant.class)
                .filter(and(
                        eq("_id", grantId),
                        eq("sessionId", oldSessionId),
                        gte("expiry", new Timestamp(System.currentTimeMillis()))
                ));

        final var entity = query.modify(
                set("sessionId", newSessionId)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        return Optional.ofNullable(entity).map(this::toModel);

    }

    @Override
    public void deleteGrantBySessionId(final String sessionId) {
        getDatastore().find(MongoOidcProviderGrant.class)
                .filter(eq("sessionId", sessionId))
                .delete();
    }

    //
    // Refresh tokens
    //

    @Override
    public OidcRefreshToken createRefreshToken(final OidcRefreshToken refreshToken) {

        final var entity = toEntity(refreshToken);

        if (entity.getId() == null) {
            entity.setId(new ObjectId().toString());
        }

        getDatastore().insert(entity);

        return toModel((MongoOidcRefreshToken) entity);

    }

    @Override
    public Optional<OidcRefreshToken> consumeRefreshToken(final String secretHash) {

        // returnDocument(AFTER): the first presenter wins; a replayed token no longer matches the status
        // guard and returns null. The expiry filter keeps an expired-but-not-yet-swept token unusable.
        final var query = getDatastore().find(MongoOidcRefreshToken.class)
                .filter(and(
                        eq("secretHash", secretHash),
                        eq("status", OidcRefreshToken.Status.ACTIVE),
                        gte("expiry", new Timestamp(System.currentTimeMillis()))
                ));

        final var entity = query.modify(
                set("status", OidcRefreshToken.Status.CONSUMED)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        return Optional.ofNullable(entity).map(this::toModel);

    }

    @Override
    public void revokeRefreshTokensForGrant(final String grantId) {

        // Iterate-and-delete-by-id, mirroring MongoSessionDao.deleteSessionsForUser: this repo's bulk deletes
        // go through per-document _id removal rather than a single query-level deleteMany.
        getDatastore().find(MongoOidcRefreshToken.class)
                .filter(eq("grantId", grantId))
                .iterator()
                .toList()
                .forEach(entity ->
                        getDatastore().find(MongoOidcRefreshToken.class)
                                .filter(eq("_id", entity.getId()))
                                .delete());

    }

    //
    // Client secret handling (salted digest, mirroring MongoUserDao's password handling)
    //

    private void hashSecret(final MongoOidcClient entity, final String clientSecret) {

        if (clientSecret == null) {
            return;
        }

        final var salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        final var digest = getPasswordDigestProvider().get();
        digest.update(salt);
        digest.update(clientSecret.getBytes(UTF_8));

        entity.setSecretSalt(salt);
        entity.setSecretHash(digest.digest());
        entity.setHashAlgorithm(digest.getAlgorithm());

    }

    /**
     * Verifies a presented client secret against the registration's stored salted digest in
     * constant time.
     *
     * @param entity the stored registration entity
     * @param clientSecret the presented secret
     * @return true if the secret matches
     */
    public boolean verifyClientSecret(final MongoOidcClient entity, final String clientSecret) {

        final var digest = getPasswordDigestProvider().get();
        digest.update(entity.getSecretSalt());
        digest.update(clientSecret.getBytes(UTF_8));

        return MessageDigest.isEqual(entity.getSecretHash(), digest.digest());

    }

    //
    // Transforms (manual, mirroring MongoJwtSigningKeyDao: internal-infrastructure DAOs avoid the
    // element-module MapperRegistry parent/child binding constraints)
    //

    private OidcClientRegistration toModel(final MongoOidcClient entity) {
        final var model = new OidcClientRegistration();
        model.setClientId(entity.getClientId());
        model.setDisplayName(entity.getDisplayName());
        model.setRedirectUris(entity.getRedirectUris());
        model.setGrantTypes(entity.getGrantTypes());
        model.setScopes(entity.getScopes());
        model.setAudiences(entity.getAudiences());
        model.setStatus(entity.getStatus());
        model.setApplicationId(entity.getApplicationId());
        model.setCreatedAt(entity.getCreatedAt());
        // The raw secret is write-only and never round-trips.
        model.setClientSecret(null);
        return model;
    }

    private MongoOidcClient toEntity(final OidcClientRegistration registration) {
        final var entity = new MongoOidcClient();
        entity.setClientId(registration.getClientId());
        entity.setDisplayName(registration.getDisplayName());
        entity.setRedirectUris(registration.getRedirectUris());
        entity.setGrantTypes(registration.getGrantTypes());
        entity.setScopes(registration.getScopes());
        entity.setAudiences(registration.getAudiences());
        entity.setStatus(registration.getStatus() == null ? OidcClientRegistration.Status.ACTIVE : registration.getStatus());
        entity.setApplicationId(registration.getApplicationId());
        entity.setCreatedAt(registration.getCreatedAt() == 0 ? System.currentTimeMillis() : registration.getCreatedAt());
        return entity;
    }

    private OidcAuthorizationCode toModel(final MongoOidcAuthorizationCode entity) {
        final var model = new OidcAuthorizationCode();
        model.setCode(entity.getCode());
        model.setClientId(entity.getClientId());
        model.setUserId(entity.getUserId());
        model.setApplicationId(entity.getApplicationId());
        model.setRedirectUri(entity.getRedirectUri());
        model.setScope(entity.getScope());
        model.setState(entity.getState());
        model.setNonce(entity.getNonce());
        model.setAuthTime(entity.getAuthTime());
        model.setCodeChallenge(entity.getCodeChallenge());
        model.setCodeChallengeMethod(entity.getCodeChallengeMethod());
        model.setStatus(entity.getStatus());
        model.setExpiry(entity.getExpiry());
        return model;
    }

    private MongoOidcAuthorizationCode toEntity(final OidcAuthorizationCode code) {
        final var entity = new MongoOidcAuthorizationCode();
        entity.setCode(code.getCode());
        entity.setClientId(code.getClientId());
        entity.setUserId(code.getUserId());
        entity.setApplicationId(code.getApplicationId());
        entity.setRedirectUri(code.getRedirectUri());
        entity.setScope(code.getScope());
        entity.setState(code.getState());
        entity.setNonce(code.getNonce());
        entity.setAuthTime(code.getAuthTime());
        entity.setCodeChallenge(code.getCodeChallenge());
        entity.setCodeChallengeMethod(code.getCodeChallengeMethod());
        entity.setStatus(code.getStatus());
        entity.setExpiry(code.getExpiry());
        return entity;
    }

    private OidcProviderGrant toModel(final MongoOidcProviderGrant entity) {
        final var model = new OidcProviderGrant();
        model.setId(entity.getId());
        model.setClientId(entity.getClientId());
        model.setUserId(entity.getUserId());
        model.setApplicationId(entity.getApplicationId());
        model.setScope(entity.getScope());
        model.setSessionId(entity.getSessionId());
        model.setExpiry(entity.getExpiry());
        return model;
    }

    private MongoOidcProviderGrant toEntity(final OidcProviderGrant grant) {
        final var entity = new MongoOidcProviderGrant();
        entity.setId(grant.getId());
        entity.setClientId(grant.getClientId());
        entity.setUserId(grant.getUserId());
        entity.setApplicationId(grant.getApplicationId());
        entity.setScope(grant.getScope());
        entity.setSessionId(grant.getSessionId());
        entity.setExpiry(grant.getExpiry());
        return entity;
    }

    private OidcRefreshToken toModel(final MongoOidcRefreshToken entity) {
        final var model = new OidcRefreshToken();
        model.setId(entity.getId());
        model.setSecretHash(entity.getSecretHash());
        model.setGrantId(entity.getGrantId());
        model.setClientId(entity.getClientId());
        model.setStatus(entity.getStatus());
        model.setExpiry(entity.getExpiry());
        return model;
    }

    private MongoOidcRefreshToken toEntity(final OidcRefreshToken token) {
        final var entity = new MongoOidcRefreshToken();
        entity.setId(token.getId());
        entity.setSecretHash(token.getSecretHash());
        entity.setGrantId(token.getGrantId());
        entity.setClientId(token.getClientId());
        entity.setStatus(token.getStatus() == null ? OidcRefreshToken.Status.ACTIVE : token.getStatus());
        entity.setExpiry(token.getExpiry());
        return entity;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public Provider<MessageDigest> getPasswordDigestProvider() {
        return passwordDigestProvider;
    }

    @Inject
    public void setPasswordDigestProvider(@Named(PASSWORD_DIGEST) Provider<MessageDigest> passwordDigestProvider) {
        this.passwordDigestProvider = passwordDigestProvider;
    }

}
