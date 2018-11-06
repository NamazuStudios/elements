/* tslint:disable */
import { Profile } from './profile';

/**
 * Associates an Amazon GameOn registration with a particular profile.  There may exist only one GameOnRegistration per Profile at a time.
 */
export interface GameOnRegistration {

  /**
   * The unique ID of this registration.
   */
  id?: string;

  /**
   * The profile associated with this GameOn registration.
   */
  profile: Profile;

  /**
   * The Amazon-issued Player Token
   */
  playerToken: string;

  /**
   * The Amazon-issued external player ID
   */
  externalPlayerId: string;
}
