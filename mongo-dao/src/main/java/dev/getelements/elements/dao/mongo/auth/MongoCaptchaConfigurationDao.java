package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.auth.MongoCaptchaConfiguration;
import dev.getelements.elements.sdk.dao.CaptchaConfigurationDao;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import jakarta.inject.Inject;

import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoCaptchaConfigurationDao implements CaptchaConfigurationDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapper;

    @Override
    public Optional<CaptchaConfiguration> findConfiguration() {
        final var entity = getDatastore().find(MongoCaptchaConfiguration.class).first();
        return Optional.ofNullable(entity).map(this::transform);
    }

    @Override
    public CaptchaConfiguration updateConfiguration(final CaptchaConfiguration configuration) {

        final var existing = getDatastore().find(MongoCaptchaConfiguration.class).first();

        if (existing == null) {
            final var entity = getBeanMapper().map(configuration, MongoCaptchaConfiguration.class);
            entity.setId(null);
            final var saved = getMongoDBUtils().perform(ds -> getDatastore().save(entity));
            return transform(saved);
        }

        final var query = getDatastore().find(MongoCaptchaConfiguration.class);
        query.filter(eq("_id", existing.getId()));

        final var builder = new UpdateBuilder();
        builder.with(set("provider", configuration.getProvider()));
        builder.with(set("siteKey", configuration.getSiteKey()));
        builder.with(set("secretKey", configuration.getSecretKey()));
        builder.with(set("enabled", configuration.isEnabled()));

        final var entity = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        return transform(entity);

    }

    private CaptchaConfiguration transform(final MongoCaptchaConfiguration entity) {
        return getBeanMapper().map(entity, CaptchaConfiguration.class);
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
