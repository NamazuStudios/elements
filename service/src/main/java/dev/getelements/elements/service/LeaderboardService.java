package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Manages instances of {@link Leaderboard}.
 * 
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.leaderboard"),
    @ModuleDefinition(
        value = "namazu.elements.service.unscoped.leaderboard",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface LeaderboardService {

    Pagination<Leaderboard> getLeaderboards(int offset, int count);

    Pagination<Leaderboard> getLeaderboards(int offset, int count, String search);

    Leaderboard getLeaderboard(String nameOrId);

    Leaderboard createLeaderboard(Leaderboard leaderboard);

    Leaderboard updateLeaderboard(String nameOrId, Leaderboard leaderboard);

    void deleteLeaderboard(String nameOrId);

}
