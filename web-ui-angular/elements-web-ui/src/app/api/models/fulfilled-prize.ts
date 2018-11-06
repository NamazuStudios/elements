/* tslint:disable */

/**
 * Corresponds to the Fulfilled Prize Response: https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfillprizelistresponse_fulfilledprize
 */
export interface FulfilledPrize {

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
  status?: 'FULFILLED';
}
