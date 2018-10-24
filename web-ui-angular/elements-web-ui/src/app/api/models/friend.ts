/* tslint:disable */
import { User } from './user';
import { Profile } from './profile';

/**
 * Represents a player's friend.  This includes the basic information of the friend as well as the friendship type, profiles he or she has across games, and
 */
export interface Friend {

  /**
   * The unique ID of the friendship.
   */
  id?: string;

  /**
   * The user assocaited with this particular friend.
   */
  user?: User;

  /**
   * The friendship type.
   */
  friendship?: 'NONE' | 'OUTGOING' | 'INCOMING' | 'MUTUAL';

  /**
   * The profiles which are associated with the friend user.
   */
  profiles?: Array<Profile>;
}
