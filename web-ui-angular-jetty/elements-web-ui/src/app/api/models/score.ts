/* tslint:disable */
import { Profile } from './profile';
export interface Score {

  /**
   * The ID of the Score
   */
  id: string;
  profile: Profile;

  /**
   * The point value of the score.
   */
  pointValue?: number;

  /**
   * The the units of measure for the points.  For example, if the points in the game were called "coins" instead of "points" this would be used to designate as such in the UI.
   */
  scoreUnits?: string;
}
