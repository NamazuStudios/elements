package com.namazustudios.socialengine.service.savedata;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.savedata.CreateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.savedata.UpdateSaveDataDocumentRequest;
import com.namazustudios.socialengine.service.SaveDataDocumentService;

public class UserSaveDataDocumentService implements SaveDataDocumentService {
    @Override
    public SaveDataDocument getSaveDataDocument(String nameOrId) {
        return null;
    }

    @Override
    public Pagination<Application> getSaveDataDocuments(int offset, int count, String query) {
        return null;
    }

    @Override
    public Pagination<Application> getSaveDataDocuments(int offset, int count, String userId, String profileId) {
        return null;
    }

    @Override
    public SaveDataDocument createSaveDataDocument(CreateSaveDataDocumentRequest saveDataDocument) {
        return null;
    }

    @Override
    public SaveDataDocument updateSaveDataDocument(String saveDataDocumentId, UpdateSaveDataDocumentRequest updateSaveDataDocumentRequest) {
        return null;
    }

    @Override
    public void deleteSaveDocument(String saveDataDocumentId) {

    }
}
