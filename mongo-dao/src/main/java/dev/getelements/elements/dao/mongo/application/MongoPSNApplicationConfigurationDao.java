package dev.getelements.elements.dao.mongo.application;

import dev.getelements.elements.dao.PSNApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.application.MongoPSNApplicationConfiguration;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.application.PSNApplicationConfiguration;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.model.application.ConfigurationCategory.PSN_PS4;
import static dev.getelements.elements.model.application.ConfigurationCategory.PSN_VITA;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.util.Arrays.asList;


/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoPSNApplicationConfigurationDao implements PSNApplicationConfigurationDao {

    private MongoApplicationDao mongoApplicationDao;

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public PSNApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final PSNApplicationConfiguration psnApplicationConfiguration) {

        validate(psnApplicationConfiguration);

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoPSNApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", false),
                eq("parent", mongoApplication),
                eq( "category", asList(PSN_PS4, PSN_VITA)),
                eq("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier())
            )
        );

        final var mongoPSNApplicationProfile = getMongoDBUtils().perform(ds ->
            query.modify(
                set("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier().trim()),
                set("client_secret", nullToEmpty(psnApplicationConfiguration.getClientSecret()).trim()),
                set("active", false),
                set( "category", psnApplicationConfiguration.getCategory()),
                set("parent", mongoApplication)
            ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationConfiguration.class);

    }

    @Override
    public PSNApplicationConfiguration getPSNApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoPSNApplicationConfiguration.class);

        query.filter(and(
                eq("active", true),
                eq("parent", mongoApplication),
                in( "category", asList(PSN_VITA, PSN_PS4))
        ));

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile = query.first();

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationConfiguration.class);

    }

    @Override
    public PSNApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId,
                                                                      final PSNApplicationConfiguration psnApplicationConfiguration) {

        validate(psnApplicationConfiguration);

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoPSNApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                in( "category", asList(PSN_VITA, PSN_PS4))
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoPSNApplicationConfiguration = getMongoDBUtils().perform(ds ->
            query.modify(
                set("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier().trim()),
                set("client_secret", nullToEmpty(psnApplicationConfiguration.getClientSecret()).trim()),
                set( "category", psnApplicationConfiguration.getCategory()),
                set("parent", mongoApplication)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoPSNApplicationConfiguration == null) {
            throw new NotFoundException("profile with ID not found: " + applicationConfigurationNameOrId);
        }

        return getBeanMapper().map(mongoPSNApplicationConfiguration, PSNApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(final String applicationNameOrId,
                                                   final String applicationConfigurationNameOrId) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoPSNApplicationConfiguration.class);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                in( "category", asList(PSN_VITA, PSN_PS4))
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoPSNApplicationConfiguration = getMongoDBUtils().perform(ds ->
            query.modify(
                set("active", false)
            ).execute(new ModifyOptions().upsert(false))
        );

        if (mongoPSNApplicationConfiguration == null) {
            throw new NotFoundException("profile with ID not found: " + applicationConfigurationNameOrId);
        }

    }

    public void validate(final PSNApplicationConfiguration psnApplicationProfile) {

        if (psnApplicationProfile == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (psnApplicationProfile.getCategory()) {
            case PSN_PS4:
            case PSN_VITA:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + psnApplicationProfile.getCategory());
        }

        getValidationHelper().validateModel(psnApplicationProfile);

    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

}
