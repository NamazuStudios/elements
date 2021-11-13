/* tslint:disable */
export interface Leaderboard {
  id?: string;

  /**
   * The unique-name of the leaderboard.  This must be unique across all leaderboards.
   */
  name: string;

  /**
   * The time strategy for the leaderboard. Current options are ALL_TIME and EPOCHAL.
   */
  timeStrategyType: string;

  /**
   * The score strategy for the leaderboard. Current options are OVERWRITE_IF_GREATER and ACCUMULATE.
   */
  scoreStrategyType: string;

  /**
   * The user-presentable name or title for for the leaderboard.
   */
  title: string;

  /**
   * The units-of measure for the score type of the leaderboard.
   */
  scoreUnits: string;

  /**
   * The time at which the leaderboard epoch intervals should begin (in ms). 
   * If null, then the leaderboard is all-time and not epochal. 
   * During creation, if this value is provided, then epochInterval must also be provided.
   * minimum: 0
   */
  firstEpochTimestamp?: number;

  /**
   * The duration for a leaderboard epoch interval (in ms). 
   * If null, then the leaderboard is all-time and not epochal. 
   * During creation, if this value is provided, then firstEpochTimestamp must also be provided.
   * minimum: 0
   */
  epochInterval?: number;


}
