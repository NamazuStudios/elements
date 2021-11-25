package com.namazustudios.socialengine.dao.mongo.savedata;

import com.namazustudios.socialengine.dao.SaveDataDocumentDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;

import java.util.Optional;

public class MongoSaveDataDocumentDao implements SaveDataDocumentDao  {
    
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
        return null;
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
    public void deleteSaveDocument(String saveDataDocumentId) {

    }
}
