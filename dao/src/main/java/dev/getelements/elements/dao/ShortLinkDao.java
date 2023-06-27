package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ShortLink;

/**
 * Manages the {@link ShortLink} instances in the database.  This deals strictly
 * with the database representation of the {@link ShortLink} object.
 *
 * Unless otherwise noted, the {@link ShortLinkDao} does not deal with the full-path
 * url.  Instances returned from this may not have a valid value set for {@link ShortLink#getShortLinkPath()},
 * and only guarantees a non-null return value for {@link ShortLink#getShortLinkPath()}. It is the responsibility
 * of the calling code to derive the full URL using the value from {@link ShortLink#getShortLinkPath()}.
 *
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
    ShortLink getShortLinkWithId(final String id);

    /**
     * Gets an instance of {@link ShortLink} using the path of the short link, as
     * returned from {@link ShortLink#getShortLinkPath()}
     *
     * @param shortLinkPath
     * @return the short link instance
     */
    ShortLink getShortLinkWithPath(final String shortLinkPath);

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
