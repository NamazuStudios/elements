/* tslint:disable */
import { Profile } from './profile';

/**
 * Represents a single one-on-one match between the current player and an opponent.  Once matched, the player will will be able to create a game against the supplied opposing player.  The server may modify or delete matches based on a variety of circumstances.
 */
export interface Match {

  /**
   * The unique ID of the match.
   */
  id?: string;

  /**
   * The scheme to use when matching with other players.
   */
  scheme: string;

  /**
   * An optional scope for the match.  For example, if the match were part of a tournament, it could be scoped to the unique ID of the tournament.
   */
  scope?: string;

  /**
   * The player requesting the match.  If not specified, then the current profile will be inferred.
   */
  player: Profile;

  /**
   * The opposing player, or null if no suitable opponent has been found.
   */
  opponent?: Profile;

  /**
   * The time of the last modification of the match.
   */
  lastUpdatedTimestamp?: number;

  /**
   * The system-assigned game ID of the match.  Null until the match is successfully made.
   */
  gameId?: string;

  /**
   * Additional arbitrary metadata that is attached to the match.
   */
  metadata?: {[key: string]: {}};
}
