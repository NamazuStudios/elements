/* tslint:disable */
import { Match } from './match';

/**
 * Defines the data necessary to enter a match.
 */
export interface GameOnEnterMatchRequest {

  /**
   * The device OS Type, used to create or reference the session.
   */
  deviceOSType?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';

  /**
   * App build type, used to create or reference the session.
   */
  appBuildType?: 'development' | 'release';

  /**
   * The player attribues, if applicable.  This may be empty or null.
   */
  playerAttributes?: {[key: string]: {}};

  /**
   * The Match
   */
  match?: Match;
}
