/* tslint:disable */
import { Profile } from './profile';

/**
 * Corresponds to the GetMatchLeaderboardResponse_LeaderboardItem:https://developer.amazon.com/docs/gameon/game-api-ref.html#getmatchleaderboardresponse_leaderboarditem
 */
export interface LeaderboardItem {
  profile?: Profile;
  externalPlayerId?: string;
  playerName?: string;
  rank?: number;
  score?: number;
  currentPlayer?: boolean;
}
