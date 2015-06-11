package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;

/**
 * A service level object for managing instances of {@link ShortLink}.  In addition to
 * manipulating the actual data as stored in the database, objects implementing this
 * interface are responsible for {@link }
 *
 * Created by patricktwohig on 6/10/15.
 */
public interface ShortLinkService {

    /**
     * Gets all short links known to the system.
     *
     * @param offset the offset in the toal data set
     * @param count the number of short links to fetch
     *
     * @return a {@link Pagination} of {@link ShortLink} objects
     */
    Pagination<ShortLink> getShortLinks(final int offset, final int count);

    /**
     * Same as {@link #getShortLinks(int, int)} only this accepts an additional
     * query strying as part of the parameters.
     *
     * @param offset the offset
     * @param count the count
     * @param query the query filter
     *
     * @return a {@link Pagination} of {@link ShortLink} objects
     */
    Pagination<ShortLink> getShortLinks(final int offset, final int count, final String query);

    /**
     * Gets a ShortLink with the given ID.
     *
     * @param id the ID
     * @return the ShortLink (or throws if not found)
     */
    ShortLink getShortLink(final String id);

    /**
     * Creates a {@link ShortLink} with the given object.
     *
     * @param link the link
     * @return the object, as it was persisted
     */
    ShortLink create(final ShortLink link);

    /**
     * Deletes the short link with the given id.
     *
     * @param id the id
     */
    void deleteShortLink(final String id);

}
