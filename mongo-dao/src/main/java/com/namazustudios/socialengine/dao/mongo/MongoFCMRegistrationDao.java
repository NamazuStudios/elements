package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.WriteResult;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.namazustudios.socialengine.dao.FCMRegistrationDao;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoFCMRegistration;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.ModifyOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.util.stream.Stream;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoFCMRegistrationDao implements FCMRegistrationDao {

    private Mapper mapper;

    private Datastore datastore;

    private MongoProfileDao mongoProfileDao;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public FCMRegistration createRegistration(final FCMRegistration fcmRegistration) {

        validate(fcmRegistration);

        final MongoProfile mongoProfile;
        mongoProfile = getMongoProfileDao().getActiveMongoProfile(fcmRegistration.getProfile());

        final MongoFCMRegistration mongoFCMRegistration = new MongoFCMRegistration();
        mongoFCMRegistration.setProfile(mongoProfile);
        mongoFCMRegistration.setRegistrationToken(fcmRegistration.getRegistrationToken());

        datastore.save(mongoFCMRegistration);

        return getMapper().map(mongoFCMRegistration, FCMRegistration.class);

    }

    @Override
    public FCMRegistration updateRegistration(final FCMRegistration fcmRegistration) {

        validate(fcmRegistration);

        final var registrationId = getMongoDBUtils().parseOrThrowNotFoundException(fcmRegistration.getId());
        final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(fcmRegistration.getProfile());

        final var query = getDatastore().find(MongoFCMRegistration.class);
        query.filter(Filters.and(eq("_id", registrationId)));

        final var mongoFCMRegistration = query.modify(
            set("profile", mongoProfile),
            set("registrationToken", fcmRegistration.getRegistrationToken())
        ).execute(new ModifyOptions().returnDocument(AFTER));

        if (mongoFCMRegistration == null) {
            throw new NotFoundException("FCM Registration not found: " + fcmRegistration.getId());
        }

        return getMapper().map(mongoFCMRegistration, FCMRegistration.class);

    }

    @Override
    public void deleteRegistration(final String fcmRegistrationId) {

        final ObjectId registrationId = getMongoDBUtils().parseOrThrowNotFoundException(fcmRegistrationId);
        final DeleteResult deleteResult = getDatastore().find(MongoFCMRegistration.class).filter(eq("_id", registrationId)).delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("FCM Registration not found: " + fcmRegistrationId);
        }

    }

    @Override
    public void deleteRegistrationWithRequestingProfile(final Profile profile, final String fcmRegistrationId) {

        final ObjectId registrationId = getMongoDBUtils().parseOrThrowNotFoundException(fcmRegistrationId);
        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);

        final Query<MongoFCMRegistration> query = getDatastore().find(MongoFCMRegistration.class);
        query.filter(Filters.and(eq("_id", registrationId), eq("profile", mongoProfile)));

        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("FCM Registration not found: " + fcmRegistrationId);
        }

    }

    public void validate(final FCMRegistration fcmRegistration) {

        getValidationHelper().validateModel(fcmRegistration);

        if (fcmRegistration.getProfile() == null) {
            throw new InvalidDataException("FCM Registration Missing Profile.");
        }

    }

    @Override
    public Stream<FCMRegistration> getRegistrationsForRecipient(final String recipientId) {
        final var recipient = getMongoProfileDao().getActiveMongoProfile(recipientId);
        final var query = getDatastore().find(MongoFCMRegistration.class);
        query.filter(eq("profile", recipient));
        return query.iterator().toList().stream().map(p -> getMapper().map(p, FCMRegistration.class));
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

}
