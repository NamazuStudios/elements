package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.savedata.SaveDataNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.savedata.CreateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.savedata.UpdateSaveDataDocumentRequest;

import java.util.Optional;

/**
 * Saves and retrieves documents from the save document system. Save documents are arbitrary data used by applications
 * for data that needs persist cross sessions and application clients.
 */
public interface SaveDataDocumentService {

    /**
     * Gets a specific save document with the supplied.
     *
     * @param saveDataDocumentId the save document ID
     * @return the save document
     */
    default SaveDataDocument getSaveDataDocument(final String saveDataDocumentId) {
        return findSaveDataDocument(saveDataDocumentId).orElseThrow(SaveDataNotFoundException::new);
    }

    /**
     * Finds a save data document with a specified id, returning an empty {@link Optional<SaveDataDocument>} if no such
     * document has been found.
     *
     * @param saveDataDocumentId the save data document id.
     * @return an {@link Optional<SaveDataDocument>}
     */
    Optional<SaveDataDocument> findSaveDataDocument(final String saveDataDocumentId);

    /**
     * Gets all save data documents, filtering by the supplied query string.
     *
     * @param offset the offset
     * @param count the number of results to return
     * @param query the query string
     * @return a {@link Pagination<SaveDataDocument>}
     */
    Pagination<SaveDataDocument> getSaveDataDocuments(int offset, int count, String query);

    /**
     * Gets all save data documents. Filtering by userId, profileId, or both.
     *
     * @param offset the offset
     * @param count the number of resluts to return
     * @param userId the userId, if blank or null this will be ignored
     * @param profileId the profileId, if blank or null this will be ignored
     * @return a {@link Pagination<SaveDataDocument>}
     */
    Pagination<SaveDataDocument> getSaveDataDocuments(int offset, int count, String userId, String profileId);

    /**
     * Gets a user-scoped saved data document by user id and slot.
     *
     * @param userId the user id
     * @param slot the slot
     * @return the {@link SaveDataDocument}, never null
     */
    SaveDataDocument getUserSaveDataDocumentBySlot(String userId, int slot);

    /**
     * Gets a profile-scoped saved data document by user id and slot.
     *
     * @param profileId the profile id
     * @param slot the slot
     * @return the {@link SaveDataDocument}, never null
     */
    SaveDataDocument getProfileSaveDataDocumentBySlot(String profileId, int slot);

    /**
     * Creates a new SaveDataDocument in the database.
     *
     * @param createSaveDataDocumentRequest the creation request
     * @return the created {@link SaveDataDocument}
     */
    SaveDataDocument createSaveDataDocument(CreateSaveDataDocumentRequest createSaveDataDocumentRequest);

    /**
     * Updates the {@link SaveDataDocument} with the supplied id and the {@link UpdateSaveDataDocumentRequest} and
     * the
     *
     * @param saveDataDocumentId the save document ID
     * @param updateSaveDataDocumentRequest the update request
     * @return the {@link SaveDataDocument} as it was written to the database
     */
    SaveDataDocument updateSaveDataDocument(String saveDataDocumentId, UpdateSaveDataDocumentRequest updateSaveDataDocumentRequest);

    /**
     * Deletes the supplied {@link SaveDataDocument}.
     *
     * @param saveDataDocumentId the identifier of the save data document
     */
    void deleteSaveDocument(String saveDataDocumentId);

}
