package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.Document;

/**
 * Represents the Identity of an object, as specified by {@link SearchableIdentity}.  This
 * is composed of three essential elements.
 *
 * <ul>
 *  <li>The concrete {@link Class<?>} representing the document type iself.</li>
 *  <li>The concrete {@link Class<?>} representing the identifier type (eg int, float, String).</li>
 *  <li>The actual identity object representing the object's unique ID.</li>
 * </ul>
 *
 * Created by patricktwohig on 5/15/15.
 */
public interface Identity<DocumentT> {

    /**
     * Gets the Document's type.  This corresponds to {@link SearchableDocument#type()}.  Note,
     * as subclasses of a document type may be stored, this may not be equal to the
     * corresponding type.
     *
     * @return the document's type.
     */
    Class<? extends DocumentT> getDocumentType();

    /**
     * Gets the object's identity type.  This corresponds to {@link SearchableIdentity#value()}.
     *
     * @return the identity type
     */
    Class<?> getIdentityType();

    /**
     * Extracts the identity value from the actual underlying {@link Document}.
     *
     * @return the identity of the document.
     */
    Object getIdentity();

    /**
     * Extracts and checks this object's identity type, returns the identity cast
     * as the proper type.
     *
     * @return this identity, cast as the correctly checked type
     *
     */
    <IdentityT> IdentityT getIdentity(Class<IdentityT> identityTClass);

}
