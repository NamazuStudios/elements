package dev.getelements.elements.dao.mongo.ucode;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.sdk.dao.UniqueCodeDao;
import dev.getelements.elements.sdk.model.ucode.UniqueCode;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import jakarta.inject.Inject;

import java.nio.CharBuffer;
import java.util.Optional;

public class MongoUniqueCodeDao implements UniqueCodeDao {


    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry mapperRegistry;

    @Override
    public UniqueCode generateCode(final GenerationParameters parameters) {

        MongoUniqueCode mongoUniqueCode = null;

        for (int i = 0; i < parameters.maxAttempts() && mongoUniqueCode == null; ++i) {

        }

        return getMapperRegistry().map(mongoUniqueCode, UniqueCode.class);

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

}
