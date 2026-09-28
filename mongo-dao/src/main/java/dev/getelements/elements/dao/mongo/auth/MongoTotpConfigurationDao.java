package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.auth.MongoTotpConfiguration;
import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import jakarta.inject.Inject;

import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoTotpConfigurationDao implements TotpConfigurationDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapper;

    @Override
    public Optional<TotpConfiguration> findConfiguration() {
        final var entity = getDatastore().find(MongoTotpConfiguration.class).first();
        return Optional.ofNullable(entity).map(this::transform);
    }

    @Override
    public TotpConfiguration updateConfiguration(final TotpConfiguration configuration) {

        final var existing = getDatastore().find(MongoTotpConfiguration.class).first();

        if (existing == null) {
            final var entity = getBeanMapper().map(configuration, MongoTotpConfiguration.class);
            entity.setId(null);
            final var saved = getMongoDBUtils().perform(ds -> getDatastore().save(entity));
            return transform(saved);
        }

        final var query = getDatastore().find(MongoTotpConfiguration.class);
        query.filter(eq("_id", existing.getId()));

        final var builder = new UpdateBuilder();
        builder.with(set("enabled", configuration.isEnabled()));

        final var entity = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        return transform(entity);

    }

    private TotpConfiguration transform(final MongoTotpConfiguration entity) {
        return getBeanMapper().map(entity, TotpConfiguration.class);
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
