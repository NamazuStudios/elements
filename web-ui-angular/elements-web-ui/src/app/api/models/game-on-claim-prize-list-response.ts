/* tslint:disable */
import { ClaimedPrize } from './claimed-prize';

/**
 * Corresponds to the GameOn Prize Claim Response: https://developer.amazon.com/docs/gameon/game-api-ref.html#claimprizelistresponse
 */
export interface GameOnClaimPrizeListResponse {

  /**
   * The GameOn Assigned external player ID.
   */
  externalPlayerId?: string;

  /**
   * A list of Prize IDs that failed to award.
   */
  failedAwardedPrizeIds?: Array<string>;

  /**
   * A list of successfully claimed prizes.
   */
  prizes?: Array<ClaimedPrize>;
}
