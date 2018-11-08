/* tslint:disable */
import { Prize } from './prize';

/**
 * Used by the GameOn API to create instances of prizes.  Used only in the Admin API.  See: https://developer.amazon.com/docs/gameon/admin-api-ref.html#addprizelistrequest
 */
export interface GameOnAddPrizeListRequest {

  /**
   * Allows for the specification of one or more Prizes when creating a prize with Amazon GameOn
   */
  prizes: Array<Prize>;
}
