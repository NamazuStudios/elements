/* tslint:disable */
import { Profile } from './profile';

/**
 * Represents a Firebase Cloud Messaging Registration Token
 */
export interface FCMRegistration {

  /**
   * The the unique id of the token stored in the database.
   */
  id?: string;

  /**
   * The actual Firebase registration.
   */
  registrationToken: string;

  /**
   * The Profile associated with this registration.
   */
  profile?: Profile;
}
