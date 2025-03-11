package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.savedata.SaveDataNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.savedata.SaveDataDocument;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

/**
 * Accesses save data documents from the database.
 */
@ElementServiceExport
public interface SaveDataDocumentDao {

    /**
     * Gets a {@link SaveDataDocument} from the supplied id.
     *
     * @param saveDataDocumentId the database-assigned unique id of the save document
     * @return the save data document, never null
     * @throws SaveDataNotFoundException if no such save document exists
     */
    default SaveDataDocument getSaveDataDocument(final String saveDataDocumentId) {
        return findSaveDataDocument(saveDataDocumentId).orElseThrow(SaveDataNotFoundException::new);
    }

    /**
     * Gets the user-scoped save data document supplying the user id and the slot.
     *
     * @param userId
     * @param slot
     * @return the {@link SaveDataDocument}
     */
    default SaveDataDocument getUserSaveDataDocumentBySlot(String userId, int slot) {
        return findUserSaveDataDocumentBySlot(userId, slot).orElseThrow(SaveDataNotFoundException::new);
    }

    /**
     * Gets the profile scoped save data document supplying the user id and the slot.
     *
     * @param profileId the profile Id
     * @param slot      the slot
     * @return the {@link SaveDataDocument}
     */
    default SaveDataDocument getProfileSaveDataDocumentBySlot(String profileId, int slot) {
        return findProfileSaveDataDocumentBySlot(profileId, slot).orElseThrow(SaveDataNotFoundException::new);
    }

    /**
     * Finds a {@link SaveDataDocument} from the supplied id.
     *
     * @param saveDataDocumentId the save data document ID
     * @return an {@link Optional<SaveDataDocument>} with the data
     */
    Optional<SaveDataDocument> findSaveDataDocument(final String saveDataDocumentId);

    /**
     * Finds the user-scoped save data document supplying the user id and the slot.
     *
     * @param userId
     * @param slot
     * @return the {@link SaveDataDocument}
     */
    Optional<SaveDataDocument> findUserSaveDataDocumentBySlot(final String userId, final int slot);

    /**
     * Finds the profile scoped save data document supplying the user id and the slot.
     *
     * @param profileId the profile Id
     * @param slot      the slot
     * @return the {@link SaveDataDocument}
     */
    Optional<SaveDataDocument> findProfileSaveDataDocumentBySlot(final String profileId, final int slot);

    /**
     * Gets all save data documents matching the supplied criteria.
     *
     * @param offset    the index of the offset
     * @param count     the count of documents immediately following the offset
     * @param userId    the user id, specify null or empty string to exclude filtering by user id
     * @param profileId the profile idk specify null or empty string to exclude filtering by profile id
     * @return a {@link Pagination<SaveDataDocument>} of all documents matching the query
     */
    Pagination<SaveDataDocument> getSaveDataDocuments(int offset, int count,
                                                      String userId, String profileId);


    /**
     * Gets all save data documents matching the supplied criteria.
     *
     * @param offset    the index of the offset
     * @param count     the count of documents immediately following the offset
     * @param userId    the user id, specify null or empty string to exclude filtering by user id
     * @param profileId the profile idk specify null or empty string to exclude filtering by profile id
     * @param query     the search query to use when filtering results.
     * @return a {@link Pagination<SaveDataDocument>} of all documents matching the query
     */
    Pagination<SaveDataDocument> getSaveDataDocuments(int offset, int count,
                                                      String userId, String profileId, String query);

    /**
     * Creates a new {@link SaveDataDocument} in the database.
     *
     * @param document the document to create
     * @return the document as it was created in the database
     */
    SaveDataDocument createSaveDataDocument(SaveDataDocument document);

    /**
     * Updates an existing {@link SaveDataDocument}. This method ignores the version check and will forcibly overwrite
     * the contents of the save file.
     *
     * @param document the document to update
     * @return the document as it was written to the database
     */
    SaveDataDocument forceUpdateSaveDataDocument(SaveDataDocument document);

    /**
     * Updates an existing {@link SaveDataDocument}. This method ensures that the supplied version matches that of the
     * database before the write takes.
     *
     * @param document the document
     * @return the document as it was written to the database
     * @throws SaveDataNotFoundException if the document was not found, or the version mismatched
     */
    SaveDataDocument checkedUpdate(SaveDataDocument document);

    /**
     * Deletes the save game document.
     *
     * @param saveDataDocumentId the save game document ID
     */
    void deleteSaveDocument(String saveDataDocumentId);

}
