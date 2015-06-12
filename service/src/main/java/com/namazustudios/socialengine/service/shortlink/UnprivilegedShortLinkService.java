package com.namazustudios.socialengine.service.shortlink;

import com.namazustudios.socialengine.dao.ShortLinkDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/12/15.
 */
public class UnprivilegedShortLinkService extends AbstractShortLinkService {

    @Inject
    private ShortLinkDao shortLinkDao;

    @Override
    public Pagination<ShortLink> getShortLinks(int offset, int count) {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<ShortLink> getShortLinks(int offset, int count, String query) {
        throw new ForbiddenException();
    }

    @Override
    public ShortLink create(ShortLink link) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteShortLink(String id) {
        throw new ForbiddenException();
    }

    @Override
    public ShortLink getShortLink(String id) {
        throw new ForbiddenException();
    }

    @Override
    public ShortLink getShortLinkWithPath(String path) {
        final ShortLink shortLink = shortLinkDao.getShortLinkWithPath(path);
        return assignFullURL(shortLink);
    }

}
