package com.namazustudios.socialengine.service.savedata;

import com.namazustudios.socialengine.dao.SaveDataDocumentDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.savedata.CreateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.savedata.UpdateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.SaveDataDocumentService;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class UserSaveDataDocumentService implements SaveDataDocumentService {

    private User user;

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
    public SaveDataDocument createSaveDataDocument(final CreateSaveDataDocumentRequest createSaveDataDocumentRequest) {

        return null;
    }

    @Override
    public SaveDataDocument updateSaveDataDocument(final String saveDataDocumentId,
                                                   final UpdateSaveDataDocumentRequest updateSaveDataDocumentRequest) {
        return null;
    }

    @Override
    public void deleteSaveDocument(final String saveDataDocumentId) {

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

}
