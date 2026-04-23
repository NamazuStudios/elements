package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.mongo.model.user.MongoPasswordResetToken;
import dev.getelements.elements.sdk.dao.PasswordResetTokenDao;
import dev.getelements.elements.sdk.model.user.PasswordResetToken;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import jakarta.inject.Inject;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import static dev.morphia.query.filters.Filters.eq;

public class MongoPasswordResetTokenDao implements PasswordResetTokenDao {

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MapperRegistry mapperRegistry;

    @Override
    public String createToken(final User user, final Timestamp expiry) {

        final var token = UUID.randomUUID().toString();
        final var mongoUser = getMongoUserDao().getMongoUser(user.getId());

        final var entity = new MongoPasswordResetToken();
        entity.setToken(token);
        entity.setUser(mongoUser);
        entity.setExpiry(expiry);

        getDatastore().insert(entity);

        return token;
    }

    @Override
    public Optional<PasswordResetToken> findToken(final String token) {

        final var entity = getDatastore().find(MongoPasswordResetToken.class)
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

        return Optional.of(getMapperRegistry().map(entity, PasswordResetToken.class));
    }

    @Override
    public void deleteToken(final String token) {
        getDatastore().find(MongoPasswordResetToken.class)
                .filter(eq("_id", token))
                .delete();
    }

    @Override
    public void deleteTokensByUser(final User user) {

        final var mongoUser = getMongoUserDao().getMongoUser(user.getId());

        getDatastore().find(MongoPasswordResetToken.class)
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
