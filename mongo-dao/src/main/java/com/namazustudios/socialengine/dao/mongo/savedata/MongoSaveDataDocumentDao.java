package com.namazustudios.socialengine.dao.mongo.savedata;

import com.namazustudios.socialengine.dao.SaveDataDocumentDao;
import com.namazustudios.socialengine.dao.mongo.*;
import com.namazustudios.socialengine.dao.mongo.model.savedata.MongoSaveDataDocument;
import com.namazustudios.socialengine.dao.mongo.model.savedata.MongoSaveDataDocumentId;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.profile.ProfileNotFoundException;
import com.namazustudios.socialengine.exception.savedata.SaveDataNotFoundException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.rt.util.Hex;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoSaveDataDocumentDao implements SaveDataDocumentDao  {

    private Mapper mapper;

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    private ValidationHelper validationHelper;

    private MongoPasswordUtils mongoPasswordUtils;

    @Override
    public Optional<SaveDataDocument> findSaveDataDocument(final String saveDataDocumentId) {
        return findMongoSaveDataDocument(saveDataDocumentId).map(msd -> getMapper().map(msd, SaveDataDocument.class));
    }

    public Optional<MongoSaveDataDocument> findMongoSaveDataDocument(final String saveDataDocumentId) {

        final MongoSaveDataDocumentId id;

        try {
            id = new MongoSaveDataDocumentId(saveDataDocumentId);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        final var mongoSaveDocument = getDatastore()
            .find(MongoSaveDataDocument.class)
            .filter(eq("_id", id))
            .first();

        return Optional.ofNullable(mongoSaveDocument);

    }

    @Override
    public Pagination<SaveDataDocument> getSaveDataDocuments(final int offset, final int count,
                                                             final String userId, final String profileId) {

        final var user = userId == null
            ? Optional.empty()
            : getMongoUserDao().findActiveMongoUser(userId);

        final var profile = profileId == null
            ? Optional.empty()
            : getMongoProfileDao().findActiveMongoProfile(profileId);

        final var query = getDatastore().find(MongoSaveDataDocument.class);

        user.ifPresent(mongoUser -> query.filter(eq("user", mongoUser)));
        profile.ifPresent(mongoProfile -> query.filter(eq("profile", mongoProfile)));

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            o -> getMapper().map(o, SaveDataDocument.class)
        );

    }

    @Override
    public Pagination<SaveDataDocument> getSaveDataDocuments(final int offset, final int count,
                                                             final String userId, final String profileId,
                                                             final String query) {
        return getSaveDataDocuments(offset, count, userId, profileId);
    }

    @Override
    public SaveDataDocument createSaveDataDocument(final SaveDataDocument document) {

        getValidationHelper().validateModel(document, Insert.class);

        final var user = document.getUser();
        final var profile = document.getProfile();

        try {

            final var msd = new MongoSaveDataDocument();

            ObjectId owner = null;

            if (user != null) {
                final var mongoUser = getMongoUserDao().getActiveMongoUser(user);
                owner = mongoUser.getObjectId();
                msd.setUser(mongoUser);
            }

            if (profile != null) {
                final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);
                owner = mongoProfile.getObjectId();
                msd.setUser(mongoProfile.getUser());
                msd.setProfile(mongoProfile);
            }

            if (owner == null) {
                throw new InvalidDataException("Must specify one owner. Neither profile nor user found.");
            }

            final var now = Instant.now();
            final var id = new MongoSaveDataDocumentId(owner, document.getSlot());

            msd.setSaveDataDocumentId(id);
            msd.setTimestamp(Timestamp.from(now));
            sign(msd, document.getContents());

            final var result = getMongoDBUtils().perform(ds -> getDatastore().save(msd));
            return getMapper().map(result, SaveDataDocument.class);

        } catch (UserNotFoundException | ProfileNotFoundException ex) {
            throw new InvalidDataException(ex);
        }

    }

    private MongoSaveDataDocument sign(final MongoSaveDataDocument mongoSaveDataDocument, final String contents) {

        final var digest = getMongoPasswordUtils().newPasswordMessageDigest();
        final byte[] bytes = contents.getBytes(getMongoPasswordUtils().getPasswordEncodingCharset());
        digest.update(bytes);

        mongoSaveDataDocument.setContents(contents);
        mongoSaveDataDocument.setVersion(digest.digest());
        mongoSaveDataDocument.setDigestAlgorithm(digest.getAlgorithm());

        return mongoSaveDataDocument;

    }

    @Override
    public SaveDataDocument forceUpdateSaveDataDocument(final SaveDataDocument document) {

        getValidationHelper().validateModel(document, Update.class);

        final MongoSaveDataDocumentId id;

        try {
            id = new MongoSaveDataDocumentId(document.getId());
        } catch (IllegalArgumentException ex) {
            throw new SaveDataNotFoundException("No save data with id: " + document.getId());
        }

        var update = sign(new UpdateBuilder(), document.getContents()).with(
            set("_id", id),
            set("timestamp", Timestamp.from(Instant.now()))
        );

        final var query = getDatastore()
            .find(MongoSaveDataDocument.class)
            .filter(eq("_id", id));

        final var result = update.execute(query, new ModifyOptions().returnDocument(AFTER));

        if (result == null)
            throw new SaveDataNotFoundException("No save data with id: " + document.getId());

        return getMapper().map(result, SaveDataDocument.class);

    }

    private UpdateBuilder sign(final UpdateBuilder update, final String contents) {

        final var digest = getMongoPasswordUtils().newPasswordMessageDigest();
        final byte[] bytes = contents.getBytes(getMongoPasswordUtils().getPasswordEncodingCharset());

        digest.update(bytes);

        return update.with(
            set("contents", contents),
            set("version", digest.digest()),
            set("digestAlgorithm", digest.getAlgorithm())
        );

    }

    @Override
    public SaveDataDocument checkedUpdate(final SaveDataDocument document) {

        getValidationHelper().validateModel(document, Update.class);

        final MongoSaveDataDocumentId id;

        try {
            id = new MongoSaveDataDocumentId(document.getId());
        } catch (IllegalArgumentException ex) {
            throw new SaveDataNotFoundException();
        }

        var update = sign(new UpdateBuilder(), document.getContents()).with(
            set("_id", id),
            set("timestamp", Timestamp.from(Instant.now()))
        );

        final var version = Hex.decode(document.getVersion());

        final var query = getDatastore()
            .find(MongoSaveDataDocument.class)
            .filter(eq("_id", id))
            .filter(eq("version", version));

        final var result = update.execute(query, new ModifyOptions().returnDocument(AFTER));

        if (result == null)
            throw new SaveDataNotFoundException("No save data with id: " + document.getId());

        return getMapper().map(result, SaveDataDocument.class);

    }

    @Override
    public void deleteSaveDocument(final String saveDataDocumentId) {

        final MongoSaveDataDocumentId id;

        try {
            id = new MongoSaveDataDocumentId(saveDataDocumentId);
        } catch (IllegalArgumentException ex) {
            return;
        }

        final var result = getDatastore()
            .find(MongoSaveDataDocument.class)
            .filter(eq("_id", id))
            .delete();

        if (result.getDeletedCount() == 0) {
            throw new SaveDataNotFoundException("No save data with id: " + saveDataDocumentId);
        }

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

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject

    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
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

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoPasswordUtils getMongoPasswordUtils() {
        return mongoPasswordUtils;
    }

    @Inject
    public void setMongoPasswordUtils(MongoPasswordUtils mongoPasswordUtils) {
        this.mongoPasswordUtils = mongoPasswordUtils;
    }

}
