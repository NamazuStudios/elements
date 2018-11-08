/* tslint:disable */
import { Prize } from './prize';

/**
 * Used by the GameOn API to fetch instances of prizes.  Used only in the Admin API.  See: https://developer.amazon.com/docs/gameon/admin-api-ref.html#getprizelistresponse
 */
export interface GameOnGetPrizeListResponse {
  prizes?: Array<Prize>;
}
