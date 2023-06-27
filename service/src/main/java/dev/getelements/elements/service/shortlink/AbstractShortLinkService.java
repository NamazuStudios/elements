package dev.getelements.elements.service.shortlink;

import dev.getelements.elements.Constants;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ShortLink;
import dev.getelements.elements.service.ShortLinkService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Provides some basic functionality to assemble the full URL for the short link.
 *
 * Created by patricktwohig on 6/12/15.
 */
public abstract class AbstractShortLinkService implements ShortLinkService {

    @Inject
    @Named(Constants.SHORT_LINK_BASE)
    private String shortLinkBase;

    /**
     * Gets the full URL given a short link's path, as returned by {@link ShortLink#getShortLinkPath()}.
     *
     * @param path the path
     * @return the full URL for the short link
     */
    public String getFullUrl(final String path) {
        return shortLinkBase.replace("/+$", "") + "/" + path;
    }

    /**
     * Given an instance of {@link ShortLink}, this assigns the full URL as built by using
     * {@link #getFullUrl(String)}.
     *
     * @param shortLink the short link object
     * @return the same short link object passed in
     */
    public ShortLink assignFullURL(final ShortLink shortLink) {
        shortLink.setShortLinkURL(getFullUrl(shortLink.getShortLinkPath()));
        return shortLink;
    }

    /**
     * Given a {@link Pagination} of {@link ShortLink} instances, this assigns the full
     * short link URL using {@link #assignFullURL(ShortLink)}.
     *
     * @param pagination the pagination of {@link ShortLink} objects.
     * @return the same pagination passed in
     */
    public Pagination<ShortLink> assignFullURLs(final Pagination<ShortLink> pagination) {

        for (final ShortLink shortLink : pagination.getObjects()) {
            assignFullURL(shortLink);
        }

        return pagination;

    }

}
