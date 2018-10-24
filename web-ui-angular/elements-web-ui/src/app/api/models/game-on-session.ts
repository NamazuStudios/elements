/* tslint:disable */
import { Profile } from './profile';

/**
 * Represents a GameOn session stored and managed by Elements.  This is designed to be a 1:1 overlap with the Amazon GameOn API with additional Elements add-ons.
 */
export interface GameOnSession {

  /**
   * The Elements assigned session ID.
   */
  id?: string;

  /**
   * The Device Operating System type.
   */
  deviceOSType: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';

  /**
   * The Amazon GameOn assigned session ID.
   */
  sessionId: string;

  /**
   * The Amazon GameOn assigned API Key.
   */
  sessionApiKey: string;

  /**
   * The time at which the session expires.
   */
  sessionExpirationDate: number;

  /**
   * The profile that owns this particualr session.
   */
  profile: Profile;

  /**
   * The appliaction build type.
   */
  appBuildType: 'development' | 'release';
}
