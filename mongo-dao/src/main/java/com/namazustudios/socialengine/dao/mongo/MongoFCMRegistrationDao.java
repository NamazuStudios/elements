package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.WriteResult;
import com.namazustudios.socialengine.dao.FCMRegistrationDao;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoFCMRegistration;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import javax.inject.Inject;
import java.util.stream.Stream;

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

        final ObjectId registrationId = getMongoDBUtils().parseOrThrowNotFoundException(fcmRegistration.getId());
        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(fcmRegistration.getProfile());

        final Query<MongoFCMRegistration> query = getDatastore().createQuery(MongoFCMRegistration.class);
        query.and(query.criteria("_id").equal(registrationId));

        final UpdateOperations<MongoFCMRegistration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoFCMRegistration.class);

        updateOperations.set("profile", mongoProfile);
        updateOperations.set("registrationToken", fcmRegistration.getRegistrationToken());

        final UpdateResults updateResults = getDatastore().update(query, updateOperations, new UpdateOptions()
                .upsert(false)
                .multi(false));

        if (updateResults.getUpdatedCount() == 0) {
            throw new NotFoundException("FCM Registration not found: " + fcmRegistration.getId());
        }

        final MongoFCMRegistration mongoFCMRegistration;
        mongoFCMRegistration = getDatastore().get(MongoFCMRegistration.class, registrationId);

        return getMapper().map(mongoFCMRegistration, FCMRegistration.class);

    }

    @Override
    public void deleteRegistration(final String fcmRegistrationId) {

        final ObjectId registrationId = getMongoDBUtils().parseOrThrowNotFoundException(fcmRegistrationId);
        final WriteResult writeResult = getDatastore().delete(MongoFCMRegistration.class, registrationId);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("FCM Registration not found: " + fcmRegistrationId);
        }

    }

    @Override
    public void deleteRegistrationWithRequestingProfile(final Profile profile, final String fcmRegistrationId) {

        final ObjectId registrationId = getMongoDBUtils().parseOrThrowNotFoundException(fcmRegistrationId);
        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);

        final Query<MongoFCMRegistration> query = getDatastore().createQuery(MongoFCMRegistration.class);

        query.and(
            query.criteria("_id").equal(registrationId),
            query.criteria("profile").equal(mongoProfile)
        );

        final WriteResult writeResult = getDatastore().delete(query);

        if (writeResult.getN() == 0) {
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

        final MongoProfile recipient = getMongoProfileDao().getActiveMongoProfile(recipientId);
        final Query<MongoFCMRegistration> query = getDatastore().createQuery(MongoFCMRegistration.class);

        query.and(
            query.criteria("profile").equal(recipient)
        );

        return query.asList().stream().map(p -> getMapper().map(p, FCMRegistration.class));
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
