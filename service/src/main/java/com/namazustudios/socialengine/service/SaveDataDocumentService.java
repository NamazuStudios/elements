package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.savedata.CreateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.savedata.UpdateSaveDataDocumentRequest;

public interface SaveDataDocumentService {

    SaveDataDocument getSaveDataDocument(final String nameOrId);

    Pagination<Application> getSaveDataDocuments(int offset, int count, String query);

    Pagination<Application> getSaveDataDocuments(int offset, int count, String userId, String profileId);

    SaveDataDocument createSaveDataDocument(CreateSaveDataDocumentRequest saveDataDocument);

    SaveDataDocument updateSaveDataDocument(String saveDataDocumentId, UpdateSaveDataDocumentRequest updateSaveDataDocumentRequest);

    void deleteSaveDocument(String saveDataDocumentId);

}
