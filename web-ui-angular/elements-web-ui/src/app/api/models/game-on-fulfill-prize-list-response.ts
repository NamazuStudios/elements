/* tslint:disable */
import { FulfilledPrize } from './fulfilled-prize';

/**
 * Corresponds to the GameOn Prize Claim Response: https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfillprizelistresponse
 */
export interface GameOnFulfillPrizeListResponse {

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
  prizes?: Array<FulfilledPrize>;
}
