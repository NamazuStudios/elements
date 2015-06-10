package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;

/**
 * Created by patricktwohig on 6/9/15.
 */
public interface ShortLinkDao {

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
     * Gets a particular short link with the given id.
     *
     * @param id the id of the short link
     *
     * @return the {@link ShortLink} object
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
     * Creates a {@link ShortLink} from the given URL.
     *
     * @param url the URL
     *
     * @return the object, as it was persisted
     */
    ShortLink createShortLinkFromURL(final String url);

    /**
     * Deletes the short link with the given id.
     *
     * @param id the id
     */
    void deleteShortLink(final String id);

}
