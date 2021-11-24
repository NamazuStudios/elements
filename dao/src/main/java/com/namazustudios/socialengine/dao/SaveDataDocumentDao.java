package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.savedata.SaveDataNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;

import java.util.Optional;

public interface SaveDataDocumentDao {

    default SaveDataDocument getSaveDataDocument(String saveDataDocumentId) {
        return findSaveDataDocument(saveDataDocumentId).orElseThrow(SaveDataNotFoundException::new);
    }

    Optional<SaveDataDocument> findSaveDataDocument(final String saveDataDocumentId);

    Pagination<SaveDataDocument> getSaveDataDocuments(int offset, int count,
                                                      String userId, String profileId);

    Pagination<SaveDataDocument> getSaveDataDocuments(int offset, int count,
                                                      String userId, String profileId, String query);

    SaveDataDocument createSaveDataDocument(SaveDataDocument document);

    SaveDataDocument forceUpdateSaveDataDocument(SaveDataDocument document);

    SaveDataDocument checkedUpdate(SaveDataDocument document);

    void deleteSaveDocument(String saveDataDocumentId);
}
