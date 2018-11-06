/* tslint:disable */

/**
 * The awarded prize model.  See: https://developer.amazon.com/docs/gameon/game-api-ref.html#getmatchdetailsresponse_awardedprize
 */
export interface AwardedPrize {

  /**
   * The awarded prize ID
   */
  awardedPrizeId?: string;

  /**
   * The awarded prize status.
   */
  status?: 'UNCLAIMED' | 'CLAIMED' | 'FULFILLED';

  /**
   * The awarded prize title.
   */
  prizeTitle?: string;
}
