/* tslint:disable */
import { Match } from './match';

/**
 * Contains the information necessary to enter a GameOn player tournament.
 */
export interface GameOnPlayerTournamentEnterRequest {

  /**
   * The device OS Type, used to create or reference the session.
   */
  deviceOSType?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';

  /**
   * App build type, used to create or reference the session.
   */
  appBuildType?: 'development' | 'release';

  /**
   * The player-defined access key for the tournament.  Only necessary if the player creating the tournament specified an access key.
   */
  accessKey?: string;
  match?: Match;
}
