/* tslint:disable */
export interface Leaderboard {
  id?: string;

  /**
   * The unique-name of the leaderboard.  This must be unique across all leaderboards.
   */
  name: string;

  /**
   * The user-presentable name or title for for the leaderboard.
   */
  title: string;

  /**
   * The units-of measure for the score type of the leaderboard.
   */
  scoreUnits: string;
}
