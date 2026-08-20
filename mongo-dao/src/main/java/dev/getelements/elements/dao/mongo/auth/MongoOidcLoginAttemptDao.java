package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.auth.MongoOidcLoginAttempt;
import dev.getelements.elements.sdk.dao.OidcLoginAttemptDao;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttempt;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttemptStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import jakarta.inject.Inject;

import java.sql.Timestamp;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static com.mongodb.client.model.ReturnDocument.BEFORE;
import static dev.getelements.elements.sdk.model.auth.OidcLoginAttemptStatus.*;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoOidcLoginAttemptDao implements OidcLoginAttemptDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapper;

    @Override
    public OidcLoginAttempt create(final OidcLoginAttempt attempt) {
        final var entity = getBeanMapper().map(attempt, MongoOidcLoginAttempt.class);
        getDatastore().insert(entity);
        return transform(entity);
    }

    @Override
    public Optional<OidcLoginAttempt> findPendingByState(final String provider, final String state) {

        final var entity = getDatastore().find(MongoOidcLoginAttempt.class)
                .filter(and(
                        eq("provider", provider),
                        eq("state", state),
                        eq("status", PENDING)
                ))
                .first();

        return notExpired(entity).map(this::transform);

    }

    @Override
    public Optional<OidcLoginAttempt> markComplete(final String state, final String sessionCreationJson) {

        final var query = getDatastore().find(MongoOidcLoginAttempt.class)
                .filter(and(eq("state", state), eq("status", PENDING)));

        final var entity = getMongoDBUtils().perform(ds -> query.modify(
                set("status", COMPLETE),
                set("sessionToken", sessionCreationJson)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER)));

        return Optional.ofNullable(entity).map(this::transform);

    }

    @Override
    public Optional<OidcLoginAttempt> markFailed(final String state, final String reason) {

        final var query = getDatastore().find(MongoOidcLoginAttempt.class)
                .filter(and(eq("state", state), eq("status", PENDING)));

        final var entity = getMongoDBUtils().perform(ds -> query.modify(
                set("status", FAILED),
                set("failureReason", reason)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER)));

        return Optional.ofNullable(entity).map(this::transform);

    }

    @Override
    public Optional<OidcLoginAttempt> markLinkReady(final String state, final String linkClaimsJson) {

        final var query = getDatastore().find(MongoOidcLoginAttempt.class)
                .filter(and(eq("state", state), eq("status", PENDING)));

        final var entity = getMongoDBUtils().perform(ds -> query.modify(
                set("status", LINK_READY),
                set("linkClaimsJson", linkClaimsJson)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER)));

        return Optional.ofNullable(entity).map(this::transform);

    }

    @Override
    public Optional<OidcLoginAttempt> findLinkReadyById(final String id) {

        final var entity = getDatastore().find(MongoOidcLoginAttempt.class)
                .filter(and(eq("_id", id), eq("status", LINK_READY)))
                .first();

        return notExpired(entity).map(this::transform);

    }

    @Override
    public Optional<OidcLoginAttempt> claimLinkReadyById(final String id) {

        final var query = getDatastore().find(MongoOidcLoginAttempt.class)
                .filter(and(eq("_id", id), eq("status", LINK_READY)));

        // returnDocument(BEFORE): the caller that wins the race gets the pre-claim document, which still has
        // linkClaimsJson populated. A concurrent second caller's modify() no longer matches (status is CLAIMED)
        // and returns null, so the claims can never be consumed twice.
        final var entity = getMongoDBUtils().perform(ds -> query.modify(
                set("status", CLAIMED),
                unset("linkClaimsJson")
        ).execute(new ModifyOptions().upsert(false).returnDocument(BEFORE)));

        return notExpired(entity).map(this::transform);

    }

    @Override
    public Optional<OidcLoginAttempt> claimCompleteById(final String id) {

        final var query = getDatastore().find(MongoOidcLoginAttempt.class)
                .filter(and(eq("_id", id), eq("status", COMPLETE)));

        // returnDocument(BEFORE): the caller that wins the race gets the pre-claim document, which still has
        // sessionToken populated. A concurrent second caller's modify() no longer matches (status is CLAIMED)
        // and returns null, so the session can never be observed twice.
        final var entity = getMongoDBUtils().perform(ds -> query.modify(
                set("status", CLAIMED),
                unset("sessionToken")
        ).execute(new ModifyOptions().upsert(false).returnDocument(BEFORE)));

        return notExpired(entity).map(this::transform);

    }

    @Override
    public Optional<OidcLoginAttempt> findPendingOrFailedById(final String id) {

        final var entity = getDatastore().find(MongoOidcLoginAttempt.class)
                .filter(eq("_id", id))
                .first();

        return notExpired(entity)
                .filter(e -> e.getStatus() == PENDING || e.getStatus() == FAILED)
                .map(this::transform);

    }

    /**
     * Mongo's TTL sweep is a background process, not synchronous with the delete condition being met, so reads
     * defensively re-check expiry rather than relying solely on the index, mirroring {@code MongoPasswordResetToken}.
     */
    private Optional<MongoOidcLoginAttempt> notExpired(final MongoOidcLoginAttempt entity) {

        if (entity == null) {
            return Optional.empty();
        }

        final var expiry = entity.getExpiry();
        final var now = new Timestamp(System.currentTimeMillis());

        return (expiry != null && expiry.before(now)) ? Optional.empty() : Optional.of(entity);

    }

    private OidcLoginAttempt transform(final MongoOidcLoginAttempt entity) {
        return getBeanMapper().map(entity, OidcLoginAttempt.class);
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapper) {
        this.beanMapper = beanMapper;
    }

}
