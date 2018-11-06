/* tslint:disable */
import { Score } from './score';
export interface Rank {

  /**
   * The position of the associated score in the result set.
   */
  position?: number;

  /**
   * The Score value for the particular rank
   */
  score: Score;
}
