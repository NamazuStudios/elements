package com.namazustudios.socialengine.service.savedata;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.SaveDataDocumentDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.exception.savedata.SaveDataNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.savedata.CreateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.savedata.UpdateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.SaveDataDocumentService;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;

public class UserSaveDataDocumentService implements SaveDataDocumentService {

    private User user;

    private ValidationHelper validationHelper;

    private UserDao userDao;

    private ProfileDao profileDao;

    private SaveDataDocumentDao saveDataDocumentDao;

    @Override
    public Optional<SaveDataDocument> findSaveDataDocument(final String saveDataDocumentId) {
        return getSaveDataDocumentDao()
            .findSaveDataDocument(saveDataDocumentId)
            .flatMap(doc ->
                Objects.equals(getUser().getId(), doc.getUser().getId())
                    ? Optional.of(doc)
                    : Optional.empty()
            );
    }

    @Override
    public Pagination<SaveDataDocument> getSaveDataDocuments(final int offset, final int count,
                                                             final String query) {
        return getSaveDataDocumentDao().getSaveDataDocuments(offset, count, getUser().getId(), null, query);
    }

    @Override
    public Pagination<SaveDataDocument> getSaveDataDocuments(final int offset, final int count,
                                                             final String userId, final String profileId) {
        return getSaveDataDocumentDao().getSaveDataDocuments(offset, count, getUser().getId(), profileId);
    }

    @Override
    public SaveDataDocument getUserSaveDataDocumentBySlot(final String userId, final int slot) {
        if (getUser().getId().equals(userId)) {
            return getSaveDataDocumentDao().getUserSaveDataDocumentBySlot(userId, slot);
        } else {
            throw new SaveDataNotFoundException();
        }
    }

    @Override
    public SaveDataDocument getProfileSaveDataDocumentBySlot(final String profileId, int slot) {

        final var profile = getProfileDao().getActiveProfile(profileId);

        if (getUser().getId().equals(profile.getUser().getId())) {
            return getSaveDataDocumentDao().getProfileSaveDataDocumentBySlot(profileId, slot);
        } else {
            throw new SaveDataNotFoundException();
        }

    }

    @Override
    public SaveDataDocument createSaveDataDocument(final CreateSaveDataDocumentRequest createSaveDataDocumentRequest) {

        getValidationHelper().validateModel(createSaveDataDocumentRequest);

        final var document = new SaveDataDocument();

        final var userId = createSaveDataDocumentRequest.getUserId();
        final var profileId = createSaveDataDocumentRequest.getProfileId();

        var user = userId == null ? null : getUserDao().getActiveUser(userId);
        var profile = profileId == null ? null : getProfileDao().getActiveProfile(profileId);

        if (user != null && profile != null) {
            if (Objects.equals(user.getId(), profile.getUser().getId()))
                throw new ConflictException("User and profile do not match.");
        } else if (profile != null) {
            document.setUser(profile.getUser());
        } else if (user != null) {
            document.setUser(user);
        } else {
            throw new InvalidDataException("Must specify either user or profile.");
        }

        if (!Objects.equals(document.getUser().getId(), getUser().getId())) {
            throw new ForbiddenException("User mismatch.");
        }

        document.setUser(user);
        document.setProfile(profile);
        document.setTimestamp(currentTimeMillis());
        document.setSlot(createSaveDataDocumentRequest.getSlot());
        document.setContents(createSaveDataDocumentRequest.getContents());

        return getSaveDataDocumentDao().createSaveDataDocument(document);

    }

    @Override
    public SaveDataDocument updateSaveDataDocument(final String saveDataDocumentId,
                                                   final UpdateSaveDataDocumentRequest updateSaveDataDocumentRequest) {

        getValidationHelper().validateModel(updateSaveDataDocumentRequest);

        final var force = updateSaveDataDocumentRequest.getForce();
        final var document = getSaveDataDocumentDao().getSaveDataDocument(saveDataDocumentId);

        if (!getUser().getId().equals(document.getUser().getId())) {
            throw new SaveDataNotFoundException("No save data with id: " + saveDataDocumentId);
        }

        document.setTimestamp(currentTimeMillis());
        document.setContents(updateSaveDataDocumentRequest.getContents());

        if (force != null && force) {
            return getSaveDataDocumentDao().forceUpdateSaveDataDocument(document);
        } else {

            final var existingVersion = updateSaveDataDocumentRequest.getVersion();

            if (existingVersion == null)
                throw new InvalidDataException("Must specify existing version.");

            document.setVersion(existingVersion);

            try {
                return getSaveDataDocumentDao().checkedUpdate(document);
            } catch (SaveDataNotFoundException nfe) {
                throw new ConflictException("Version mismatch.", nfe);
            }

        }

    }

    @Override
    public void deleteSaveDocument(final String saveDataDocumentId) {
        getSaveDataDocumentDao().deleteSaveDocument(saveDataDocumentId);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public SaveDataDocumentDao getSaveDataDocumentDao() {
        return saveDataDocumentDao;
    }

    @Inject
    public void setSaveDataDocumentDao(SaveDataDocumentDao saveDataDocumentDao) {
        this.saveDataDocumentDao = saveDataDocumentDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

}
