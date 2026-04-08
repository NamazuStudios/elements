package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.mongo.model.MongoUidVerificationToken;
import dev.getelements.elements.sdk.dao.UidVerificationTokenDao;
import dev.getelements.elements.sdk.model.user.UidVerificationToken;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import jakarta.inject.Inject;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import static dev.morphia.query.filters.Filters.eq;

public class MongoUidVerificationTokenDao implements UidVerificationTokenDao {

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MapperRegistry mapperRegistry;

    @Override
    public String createToken(final User user, final String scheme, final String uidId, final Timestamp expiry) {
        final var token = UUID.randomUUID().toString();
        final var mongoUser = getMongoUserDao().getMongoUser(user.getId());

        final var entity = new MongoUidVerificationToken();
        entity.setToken(token);
        entity.setUser(mongoUser);
        entity.setScheme(scheme);
        entity.setUidId(uidId);
        entity.setExpiry(expiry);

        getDatastore().insert(entity);
        return token;
    }

    @Override
    public Optional<UidVerificationToken> findToken(final String token) {
        final var entity = getDatastore().find(MongoUidVerificationToken.class)
                .filter(eq("_id", token))
                .first();

        if (entity == null) {
            return Optional.empty();
        }

        // MongoDB TTL index handles expiry cleanup, but check here for safety
        final var now = new Timestamp(System.currentTimeMillis());
        if (entity.getExpiry() != null && entity.getExpiry().before(now)) {
            return Optional.empty();
        }

        return Optional.of(getMapperRegistry().map(entity, UidVerificationToken.class));
    }

    @Override
    public void deleteToken(final String token) {
        getDatastore().find(MongoUidVerificationToken.class)
                .filter(eq("_id", token))
                .delete();
    }

    @Override
    public void deleteTokensByUser(final User user) {
        final var mongoUser = getMongoUserDao().getMongoUser(user.getId());
        getDatastore().find(MongoUidVerificationToken.class)
                .filter(eq("user", mongoUser))
                .delete(new DeleteOptions().multi(true));
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

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

}
