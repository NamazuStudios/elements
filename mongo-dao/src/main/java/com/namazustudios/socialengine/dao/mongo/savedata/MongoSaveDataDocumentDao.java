package com.namazustudios.socialengine.dao.mongo.savedata;

import com.namazustudios.socialengine.dao.SaveDataDocumentDao;
import com.namazustudios.socialengine.dao.mongo.MongoPasswordUtils;
import com.namazustudios.socialengine.dao.mongo.MongoProfileDao;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.model.savedata.MongoSaveDataDocument;
import com.namazustudios.socialengine.dao.mongo.model.savedata.MongoSaveDataDocumentId;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.profile.ProfileNotFoundException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class MongoSaveDataDocumentDao implements SaveDataDocumentDao  {

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    private ValidationHelper validationHelper;

    private MongoPasswordUtils mongoPasswordUtils;

    @Override
    public Optional<SaveDataDocument> findSaveDataDocument(final String saveDataDocumentId) {
        return Optional.empty();
    }

    @Override
    public Pagination<SaveDataDocument> getSaveDataDocuments(final int offset, final int count,
                                                             final String userId, final String profileId) {
        return null;
    }

    @Override
    public Pagination<SaveDataDocument> getSaveDataDocuments(final int offset, final int count,
                                                             final String userId, final String profileId,
                                                             final String query) {
        return null;
    }

    @Override
    public SaveDataDocument createSaveDataDocument(final SaveDataDocument document) {
        getValidationHelper().validateModel(document, Insert.class);

        final var user = document.getUser();
        final var profile = document.getProfile();

        try {
            final var mongoSaveDataDocument = new MongoSaveDataDocument();

            ObjectId owner = null;

            if (user != null) {
                final var mongoUser = getMongoUserDao().getActiveMongoUser(user);
                owner = mongoUser.getObjectId();
                mongoSaveDataDocument.setUser(mongoUser);
            }

            if (profile != null) {
                final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);
                owner = mongoProfile.getObjectId();
                mongoSaveDataDocument.setProfile(mongoProfile);
            }

            if (owner == null) {
                throw new InvalidDataException("Must specify one owner. Neither profile nor user found.");
            }

            final var now = Instant.now();
            final var id = new MongoSaveDataDocumentId(owner, document.getSlot());

            mongoSaveDataDocument.setId(id);
            mongoSaveDataDocument.setTimestamp(Timestamp.from(now));
            generateSignature(mongoSaveDataDocument, document.getContents());

        } catch (UserNotFoundException | ProfileNotFoundException ex) {
            throw new InvalidDataException(ex);
        }

        return null;
    }

    private void generateSignature(final MongoSaveDataDocument mongoSaveDataDocument, final String contents) {

        final var digest = getMongoPasswordUtils().newPasswordMessageDigest();

        final byte[] bytes = contents.getBytes(getMongoPasswordUtils().getPasswordEncodingCharset());
        digest.update(bytes);

        mongoSaveDataDocument.setVersion(digest.digest());
        mongoSaveDataDocument.setDigestAlgorithm(digest.getAlgorithm());

    }

    @Override
    public SaveDataDocument forceUpdateSaveDataDocument(final SaveDataDocument document) {
        return null;
    }

    @Override
    public SaveDataDocument checkedUpdate(final SaveDataDocument document) {
        return null;
    }

    @Override
    public void deleteSaveDocument(final String saveDataDocumentId) {

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
