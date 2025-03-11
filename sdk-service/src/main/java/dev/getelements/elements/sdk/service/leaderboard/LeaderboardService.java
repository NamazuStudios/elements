package dev.getelements.elements.sdk.service.leaderboard;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages instances of {@link Leaderboard}.
 * 
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface LeaderboardService {

    Pagination<Leaderboard> getLeaderboards(int offset, int count);

    Pagination<Leaderboard> getLeaderboards(int offset, int count, String search);

    Leaderboard getLeaderboard(String nameOrId);

    Leaderboard createLeaderboard(Leaderboard leaderboard);

    Leaderboard updateLeaderboard(String nameOrId, Leaderboard leaderboard);

    void deleteLeaderboard(String nameOrId);

}
