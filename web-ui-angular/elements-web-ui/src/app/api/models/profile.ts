/* tslint:disable */
import { User } from './user';
import { Application } from './application';
export interface Profile {

  /**
   * The unique ID of the profile itself.
   */
  id?: string;

  /**
   * The User associated with this Profile.
   */
  user: User;

  /**
   * The Application associated with this Profile.
   */
  application: Application;

  /**
   * A URL to the image of the profile.  (ie the User's Avatar).
   */
  imageUrl?: string;

  /**
   * A non-unique display name for this profile.
   */
  displayName: string;
}
