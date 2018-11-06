/* tslint:disable */
import { GameOnPrizeBundle } from './game-on-prize-bundle';

/**
 * Represents a GameOn Match.  Maps direcly to the Amazon GameOn APIs.  Contains slightly lessinformation than its detail counterpart.
 */
export interface GameOnMatchSummary {

  /**
   * The date the tournament begins.
   */
  dateStart?: number;

  /**
   * The GameOn assigned match ID.
   */
  matchId?: string;

  /**
   * The remaining attempts in this particular match.
   */
  attemptsRemaining?: number;

  /**
   * The title of the tournament
   */
  title?: string;

  /**
   * The subtitle of the tournament.
   */
  subtitle?: string;

  /**
   * True if the player can enter, false otherwise.
   */
  canEnter?: boolean;

  /**
   * The GameOn assigned tournament ID.
   */
  tournamentId?: string;

  /**
   * The date the tournament ends.
   */
  dateEnd?: number;

  /**
   * The image URL for the tournament.
   */
  imageUrl?: string;

  /**
   * The nubmer of matches per player.
   */
  matchesPerPlayer?: number;

  /**
   * The number of attempts a player can make per match.
   */
  playerAttemtpsPerMatch?: number;

  /**
   * The number of players per match.
   */
  playersPerMatch?: number;

  /**
   * The detailed listing of prize bundles.
   */
  prizeBundles?: Array<GameOnPrizeBundle>;
}
