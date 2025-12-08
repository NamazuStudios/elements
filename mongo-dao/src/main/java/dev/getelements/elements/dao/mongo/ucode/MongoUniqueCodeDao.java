package dev.getelements.elements.dao.mongo.ucode;

import com.mongodb.MongoWriteException;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.sdk.dao.UniqueCodeDao;
import dev.getelements.elements.sdk.model.exception.TooBusyException;
import dev.getelements.elements.sdk.model.ucode.UniqueCode;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.util.OffensiveWordFilter;
import dev.getelements.elements.sdk.util.UniqueCodeGenerator;
import dev.morphia.Datastore;
import jakarta.inject.Inject;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;

public class MongoUniqueCodeDao implements UniqueCodeDao {

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry mapperRegistry;

    private MongoUserDao userDao;

    private MongoProfileDao profileDao;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final OffensiveWordFilter OFFENSIVE_WORD_FILTER = new OffensiveWordFilter.Builder()
            .addDefaultWords()
            .ignoringCase()
            .build();

    @Override
    public UniqueCode generateCode(final GenerationParameters parameters) {

        final UniqueCodeGenerator generator = new UniqueCodeGenerator.Builder()
                .withRandom(SECURE_RANDOM)
                .rejecting(DictionaryWords::isDictionaryWord)
                .rejectingOffensiveWords(OFFENSIVE_WORD_FILTER)
                .build();

        final var mongoUser = parameters
                .userOptional()
                .map(User::getId)
                .flatMap(getUserDao()::findMongoUser)
                .orElse(null);

        final var mongoProfile = parameters
                .profileOptional()
                .flatMap(getProfileDao()::findActiveMongoProfile)
                .orElse(null);

        return generator
                .tryComputeWithUniqueCode(parameters.length(), parameters.maxAttempts(), code -> {
                    final var expiry = new Timestamp(currentTimeMillis() + parameters.timeout());

                    final var mongoUniqueCode = new MongoUniqueCode();
                    mongoUniqueCode.setId(code);
                    mongoUniqueCode.setUser(mongoUser);
                    mongoUniqueCode.setProfile(mongoProfile);
                    mongoUniqueCode.setExpiry(expiry);
                    mongoUniqueCode.setLinger(parameters.linger());
                    mongoUniqueCode.setTimeout(parameters.timeout());

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
        return Optional.empty();
    }

    public Optional<MongoUniqueCode> findMongoCode(final String code) {
        return Optional.empty();
    }

    @Override
    public void resetTimeout(final String code, final long timeout) {

    }

    @Override
    public void releaseCode(final String code) {

    }

    @Override
    public boolean tryReleaseCode(final String code) {
        return false;
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

}
