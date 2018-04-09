package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.RankDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.model.profile.Profile;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;

public class MongoRankDao implements RankDao {

    private Datastore datastore;

    @Override
    public Pagination<Rank> getRanksForGlobal(final String leaderboardNameOrId,
                                              final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<Rank> getRanksForGlobalRelative(final String leaderboardNameOrId, final String profileId,
                                                      final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final String leaderboardNameOrId, final Profile profileId,
                                               final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId, final Profile profileId,
                                                       final int offset, final int count) {
        return null;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }


}
