/* tslint:disable */
import { LeaderboardItem } from './leaderboard-item';

/**
 * Corresponds to the GetMatchLeaderboardResponse: https://developer.amazon.com/docs/gameon/game-api-ref.html#getmatchleaderboardresponse
 */
export interface GameOnGetMatchLeaderboardResponse {

  /**
   * The current player's rank.
   */
  currentPlayer?: LeaderboardItem;

  /**
   * List of leaderboard items.
   */
  leaderboard?: Array<LeaderboardItem>;

  /**
   * The neighboring leadeerboard items.
   */
  neighbors?: Array<LeaderboardItem>;
}
