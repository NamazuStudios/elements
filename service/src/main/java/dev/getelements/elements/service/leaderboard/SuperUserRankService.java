package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.model.Tabulation;
import dev.getelements.elements.model.leaderboard.RankRow;

public class SuperUserRankService extends UserRankService {

    @Override
    public Tabulation<RankRow> getRanksForGlobalTabular(final String leaderboardNameOrId, final long leaderboardEpoch) {
        return getRankDao().getRanksForGlobalTabular(leaderboardNameOrId, leaderboardEpoch);
    }


}
