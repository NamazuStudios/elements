package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.dao.TotpDao;
import dev.getelements.elements.sdk.model.auth.TotpUserState;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.pullAll;

/**
 * Stores TOTP enrollment state directly on the {@code user} collection's documents (mirrors where the
 * password hash/salt already live), via targeted partial updates rather than the full-document mapping
 * {@link MongoUserDao} uses elsewhere -- so this never risks clobbering unrelated concurrent user writes.
 */
public class MongoTotpDao implements TotpDao {

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    @Override
    public Optional<TotpUserState> findState(final String userId) {
        // A user document always exists once an account is created, so presence alone can't signal
        // enrollment -- only report a state once a secret (pending or confirmed) has actually been set.
        return getMongoUserDao().findMongoUser(userId)
                .filter(mongoUser -> mongoUser.getTotpSecret() != null)
                .map(this::transform);
    }

    @Override
    public TotpUserState beginEnrollment(final String userId, final String secret) {

        final var mongoUser = getMongoUserDao().getMongoUser(userId);

        final var query = getDatastore().find(MongoUser.class).filter(eq("_id", mongoUser.getObjectId()));

        final var builder = new UpdateBuilder();
        builder.with(set("totpSecret", secret));
        builder.with(set("totpEnabled", false));
        builder.with(set("totpRecoveryCodeHashes", List.of()));

        final var entity = builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

        return transform(entity);

    }

    @Override
    public TotpUserState confirmEnrollment(final String userId, final List<String> hashedRecoveryCodes) {

        final var mongoUser = getMongoUserDao().getMongoUser(userId);

        final var query = getDatastore().find(MongoUser.class).filter(eq("_id", mongoUser.getObjectId()));

        final var builder = new UpdateBuilder();
        builder.with(set("totpEnabled", true));
        builder.with(set("totpRecoveryCodeHashes", hashedRecoveryCodes));

        final var entity = builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

        return transform(entity);

    }

    @Override
    public void disable(final String userId) {

        final var mongoUser = getMongoUserDao().getMongoUser(userId);

        final var query = getDatastore().find(MongoUser.class).filter(eq("_id", mongoUser.getObjectId()));

        final var builder = new UpdateBuilder();
        builder.with(set("totpEnabled", false));
        builder.with(set("totpSecret", (String) null));
        builder.with(set("totpRecoveryCodeHashes", List.of()));

        builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

    }

    @Override
    public boolean consumeRecoveryCode(final String userId, final String hashedCode) {

        final var mongoUser = getMongoUserDao().getMongoUser(userId);

        // Only pull (and report success) if the code is actually present -- filtering the query on it makes
        // the whole find-and-update atomic, so the same code can never be consumed twice by concurrent calls.
        final var query = getDatastore().find(MongoUser.class).filter(and(
                eq("_id", mongoUser.getObjectId()),
                eq("totpRecoveryCodeHashes", hashedCode)
        ));

        final var builder = new UpdateBuilder();
        builder.with(pullAll("totpRecoveryCodeHashes", List.of(hashedCode)));

        final var entity = builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

        return entity != null;

    }

    private TotpUserState transform(final MongoUser mongoUser) {
        final var state = new TotpUserState();
        state.setUserId(mongoUser.getObjectId().toString());
        state.setSecret(mongoUser.getTotpSecret());
        state.setEnabled(mongoUser.isTotpEnabled());
        state.setRecoveryCodeHashes(mongoUser.getTotpRecoveryCodeHashes());
        return state;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

}
