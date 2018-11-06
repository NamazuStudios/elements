/* tslint:disable */
import { GameOnTournamentDetail } from './game-on-tournament-detail';
import { AwardedPrize } from './awarded-prize';

/**
 * Represents a GameOn Match.  Maps direclty to the Amazon GameOn APIs.  Contains slightly moreinformation than its summary counterpart.
 */
export interface GameOnMatchDetail {

  /**
   * The GameOn assigned match ID.
   */
  matchId?: string;

  /**
   * The details of the associated tournament.
   */
  tournamentDetails?: GameOnTournamentDetail;

  /**
   * Prizes awarded to the player.
   */
  awardedPrizes?: Array<AwardedPrize>;

  /**
   * True if the player can enter, false otherwise.
   */
  canEnter?: boolean;

  /**
   * The last score recorded for the match.
   */
  lastScore?: number;

  /**
   * The date at which the last score was made.
   */
  lastScoreDate?: number;

  /**
   * The player's overall score for the match.
   */
  score?: number;

  /**
   * The date the score was submitted.
   */
  scoreDate?: number;
}
