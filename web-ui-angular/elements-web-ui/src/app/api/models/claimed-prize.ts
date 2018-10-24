/* tslint:disable */

/**
 * Corresponds to the Claimed Prize Response: https://developer.amazon.com/docs/gameon/game-api-ref.html#claimprizelistresponse_claimedprize
 */
export interface ClaimedPrize {

  /**
   * The GameOn match ID that was used to claim this prize.
   */
  matchId?: string;

  /**
   * The prize info, corresponds to the prize info when the prize was created.
   */
  prizeInfo?: string;

  /**
   * The prize info type.
   */
  prizeInfoType?: 'VENDOR' | 'AMAZON_PHYSICAL';

  /**
   * The claimed prize status.
   */
  status?: 'CLAIMED' | 'RECLAIMED';
}
