/* tslint:disable */
import { GameOnPrizeBundle } from './game-on-prize-bundle';

/**
 * A more detailed response on a particular GameOn tournament.  Contains the summary and someadditional fields as well.
 */
export interface GameOnTournamentDetail {

  /**
   * The image URL for the tournament.
   */
  imageUrl?: string;

  /**
   * The GameOn assigned tournament ID.
   */
  tournamentId?: string;

  /**
   * The subtitle of the tournament.
   */
  subtitle?: string;

  /**
   * True if the player can enter, false otherwise.
   */
  canEnter?: boolean;

  /**
   * The date the tournament begins.
   */
  dateStart?: number;

  /**
   * The date the tournament ends.
   */
  dateEnd?: number;

  /**
   * The title of the tournament
   */
  title?: string;

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

  /**
   * The score type (eg highest or lowest)
   */
  scoreType?: string;

  /**
   * The win type (eg single or cumulative)
   */
  winType?: string;
}
