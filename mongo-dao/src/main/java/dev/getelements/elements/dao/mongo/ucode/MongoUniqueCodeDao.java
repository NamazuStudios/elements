package dev.getelements.elements.dao.mongo.ucode;

import com.mongodb.MongoWriteException;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.sdk.dao.UniqueCodeDao;
import dev.getelements.elements.sdk.model.exception.TooBusyException;
import dev.getelements.elements.sdk.model.ucode.UniqueCode;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.util.UniqueCodeGenerator;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import jakarta.inject.Inject;

import java.sql.Timestamp;
import java.util.Optional;

import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.gt;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.System.currentTimeMillis;

public class MongoUniqueCodeDao implements UniqueCodeDao {

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry mapperRegistry;

    private MongoUserDao userDao;

    private MongoProfileDao profileDao;

    private UniqueCodeGenerator uniqueCodeGenerator;

    @Override
    public UniqueCode generateCode(final GenerationParameters parameters) {

        final var mongoUser = parameters
                .userOptional()
                .map(User::getId)
                .flatMap(getUserDao()::findMongoUser)
                .orElse(null);

        final var mongoProfile = parameters
                .profileOptional()
                .flatMap(getProfileDao()::findActiveMongoProfile)
                .orElse(null);

        return getUniqueCodeGenerator()
                .tryComputeWithUniqueCode(parameters.length(), parameters.maxAttempts(), code -> {
                    final var expiry = new Timestamp(currentTimeMillis() + parameters.timeout());

                    final var mongoUniqueCode = new MongoUniqueCode();
                    mongoUniqueCode.setId(code);
                    mongoUniqueCode.setUser(mongoUser);
                    mongoUniqueCode.setProfile(mongoProfile);
                    mongoUniqueCode.setExpiry(expiry);
                    mongoUniqueCode.setLinger(parameters.linger());
                    mongoUniqueCode.setTimeout(parameters.timeout());
                    mongoUniqueCode.setActive(true);

                    try {
                        getDatastore().insert(mongoUniqueCode);
                        return Optional.of(mongoUniqueCode);
                    } catch (MongoWriteException ex) {
                        return Optional.empty();
                    }

                })
                .map(muc -> getMapperRegistry().map(muc, UniqueCode.class))
                .orElseThrow(TooBusyException::new);

    }

    @Override
    public Optional<UniqueCode> findCode(final String code) {
        return findMongoCode(code)
                .map(muc -> getMapperRegistry().map(muc, UniqueCode.class));
    }

    public Optional<MongoUniqueCode> findMongoCode(final String code) {
        return getActiveCodeQuery(code)
                .stream()
                .findFirst();
    }

    @Override
    public boolean tryResetTimeout(final String code) {

        final var query = getActiveCodeQuery(code);

        return query
                .stream()
                .findFirst()
                .map(mc -> {

                    final var expiry = new Timestamp(currentTimeMillis() + mc.getTimeout());
                    final var updates = new UpdateBuilder()
                            .with(set("expiry", expiry))
                            .execute(query, new UpdateOptions());

                    return updates.getModifiedCount() > 0;

                })
                .orElse(false);

    }

    @Override
    public boolean tryReleaseCode(final String code) {

        final var query = getActiveCodeQuery(code);

        return query
                .stream()
                .findFirst()
                .map(mc -> {

                    final var expiry = new Timestamp(currentTimeMillis() + mc.getLinger());
                    final var updates = new UpdateBuilder()
                            .with(set("expiry", expiry))
                            .with(set("active", false))
                            .execute(query, new UpdateOptions());

                    return updates.getModifiedCount() > 0;

                })
                .orElse(false);

    }

    public Query<MongoUniqueCode> getActiveCodeQuery(final String code) {

        final var now = new Timestamp(currentTimeMillis());

        return getDatastore()
                .find(MongoUniqueCode.class)
                .filter(
                        eq("_id", code),
                        gt("expiry", now),
                        eq("active", true)
                );

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public MongoUserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(MongoUserDao userDao) {
        this.userDao = userDao;
    }

    public MongoProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(MongoProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public UniqueCodeGenerator getUniqueCodeGenerator() {
        return uniqueCodeGenerator;
    }

    @Inject
    public void setUniqueCodeGenerator(UniqueCodeGenerator uniqueCodeGenerator) {
        this.uniqueCodeGenerator = uniqueCodeGenerator;
    }

}
