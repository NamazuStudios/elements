/* tslint:disable */
import { Prize } from './prize';

/**
 * Used by the GameOn API to return instances of prizes that were created or failed to create.  Used only in the Admin API.  See: https://developer.amazon.com/docs/gameon/admin-api-ref.html#addprizelistrequest
 */
export interface GameOnAddPrizeListResponse {
  addedPrizes?: Array<Prize>;
  failedPrizes?: Array<Prize>;
}
