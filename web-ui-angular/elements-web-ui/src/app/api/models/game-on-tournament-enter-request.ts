/* tslint:disable */
import { Match } from './match';

/**
 * The data required to post a new entry into a tournament.
 */
export interface GameOnTournamentEnterRequest {

  /**
   * The device OS Type, used to create or reference the session.
   */
  deviceOSType?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';

  /**
   * App build type, used to create or reference the session.
   */
  appBuildType?: 'development' | 'release';

  /**
   * An optional access key to enter the tournament.  Only used if the tournament was created with a special code requiring entry.
   */
  accessKey?: string;

  /**
   * The player attribues, if applicable.  This may be empty or null.
   */
  playerAttributes?: {[key: string]: string};

  /**
   * The Match
   */
  match?: Match;
}
