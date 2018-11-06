/* tslint:disable */
import { Match } from './match';

/**
 * The result returned from entering a GameOn Match
 */
export interface GameOnEnterMatchResponse {

  /**
   * The GameOn Match ID that was created in response to the request.
   */
  matchId?: string;

  /**
   * The GameOn Tournament ID that was entered in response to the request.
   */
  tournamentId?: string;

  /**
   * The player's remaining attempts in the tournament.
   */
  attemptsRemaining?: number;

  /**
   * The tournament metadata that was used to create the tournament.  May be null.
   */
  metadata?: string;

  /**
   * The Match created as part of the tournament entry.
   */
  match?: Match;
}
