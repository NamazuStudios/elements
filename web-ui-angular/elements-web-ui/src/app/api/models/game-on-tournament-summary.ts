/* tslint:disable */
import { GameOnPrizeBundle } from './game-on-prize-bundle';

/**
 * Maps to the response from a GameOn Tournament object from the AWS GameOn API.
 */
export interface GameOnTournamentSummary {

  /**
   * The date the tournament ends.
   */
  dateEnd?: number;

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
   * The title of the tournament
   */
  title?: string;

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
