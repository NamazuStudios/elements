package com.namazustudios.socialengine.service.shortlink;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.ShortLinkDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;
import com.namazustudios.socialengine.service.ShortLinkService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by patricktwohig on 6/10/15.
 */
public class SuperuserShortLinkService implements ShortLinkService {

    @Inject
    private ShortLinkDao shortLinkDao;

    @Inject
    @Named(Constants.SHORT_LINK_BASE)
    private String shortLinkBase;

    @Override
    public Pagination<ShortLink> getShortLinks(int offset, int count) {
        final Pagination<ShortLink> pagination = shortLinkDao.getShortLinks(offset, count);
        return assignFullURLs(pagination);
    }

    @Override
    public Pagination<ShortLink> getShortLinks(int offset, int count, String query) {
        final Pagination<ShortLink> pagination = shortLinkDao.getShortLinks(offset, count, query);
        return assignFullURLs(pagination);
    }

    @Override
    public ShortLink getShortLink(String id) {
        final ShortLink shortLink = shortLinkDao.getShortLinkWithId(id);
        return assignFullURL(shortLink);
    }

    @Override
    public ShortLink create(ShortLink link) {
        final ShortLink shortLink = shortLinkDao.create(link);
        return assignFullURL(shortLink);
    }

    @Override
    public void deleteShortLink(String id) {
        shortLinkDao.deleteShortLink(id);
    }

    public String getFullUrl(final String path) {
        return shortLinkBase.replace("/+$", "") + path;
    }

    public Pagination<ShortLink> assignFullURLs(final Pagination<ShortLink> pagination) {

        for (final ShortLink shortLink : pagination.getObjects()) {
            assignFullURL(shortLink);
        }

        return pagination;

    }

    public ShortLink assignFullURL(final ShortLink shortLink) {
        shortLink.setShortLinkURL(getFullUrl(shortLink.getShortLinkPath()));
        return shortLink;
    }

}
