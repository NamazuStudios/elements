package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.dao.mongo.model.auth.MongoTotpLoginChallenge;
import dev.getelements.elements.sdk.dao.TotpLoginChallengeDao;
import dev.getelements.elements.sdk.model.auth.TotpLoginChallenge;
import dev.morphia.Datastore;
import jakarta.inject.Inject;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Optional;

import static dev.morphia.query.filters.Filters.eq;

public class MongoTotpLoginChallengeDao implements TotpLoginChallengeDao {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int ID_BYTES = 32;

    private Datastore datastore;

    @Override
    public String createChallenge(final String userId,
                                   final String profileId,
                                   final String profileSelector,
                                   final String applicationNameOrId,
                                   final Timestamp expiry) {

        final var entity = new MongoTotpLoginChallenge();
        entity.setId(randomId());
        entity.setUserId(userId);
        entity.setProfileId(profileId);
        entity.setProfileSelector(profileSelector);
        entity.setApplicationNameOrId(applicationNameOrId);
        entity.setExpiry(expiry);

        getDatastore().insert(entity);

        return entity.getId();

    }

    @Override
    public Optional<TotpLoginChallenge> findAndConsume(final String id) {

        final var query = getDatastore().find(MongoTotpLoginChallenge.class).filter(eq("_id", id));
        final var entity = query.first();

        if (entity == null) {
            return Optional.empty();
        }

        query.delete();

        // The TTL index handles routine expiry cleanup, but check here too in case it hasn't reaped this
        // document yet -- an expired challenge must never be usable just because it's still physically present.
        final var now = new Timestamp(System.currentTimeMillis());

        if (entity.getExpiry() != null && entity.getExpiry().before(now)) {
            return Optional.empty();
        }

        return Optional.of(transform(entity));

    }

    private TotpLoginChallenge transform(final MongoTotpLoginChallenge entity) {
        final var challenge = new TotpLoginChallenge();
        challenge.setId(entity.getId());
        challenge.setUserId(entity.getUserId());
        challenge.setProfileId(entity.getProfileId());
        challenge.setProfileSelector(entity.getProfileSelector());
        challenge.setApplicationNameOrId(entity.getApplicationNameOrId());
        challenge.setExpiry(entity.getExpiry());
        return challenge;
    }

    private static String randomId() {
        final var bytes = new byte[ID_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
