package dev.getelements.elements.service.match;

import dev.getelements.elements.sdk.dao.MultiMatchDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.service.match.MultiMatchService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperuserMultiMatchService implements MultiMatchService {

    private static final Logger logger = LoggerFactory.getLogger(SuperuserMultiMatchService.class);

    private MultiMatchDao multiMatchDao;

    @Override
    public MultiMatch getMatch(String matchId) {
        return getMultiMatchDao().getMultiMatch(matchId);
    }

    @Override
    public Pagination<MultiMatch> getMatches(final int offset, final int count) {
        return getMultiMatchDao().getMultiMatches(offset, count);
    }

    @Override
    public Pagination<MultiMatch> getMatches(final int offset, final int count, final String search) {
        return getMultiMatchDao().getMultiMatches(offset, count, search);
    }

    @Override
    public MultiMatch createMatch(final MultiMatch match) {
        return getMultiMatchDao().createMultiMatch(match);
    }

    @Override
    public MultiMatch updateMatch(final String matchId, final MultiMatch match) {
        return getMultiMatchDao().updateMultiMatch(matchId, match);
    }

    @Override
    public void deleteMatch(final String matchId) {
        getMultiMatchDao().deleteMultiMatch(matchId);
    }

    @Override
    public void deleteAllMatches() {
        getMultiMatchDao().deleteAllMultiMatches();
    }

    public MultiMatchDao getMultiMatchDao() {
        return multiMatchDao;
    }

    @Inject
    public void setMultiMatchDao(MultiMatchDao multiMatchDao) {
        this.multiMatchDao = multiMatchDao;
    }
}
