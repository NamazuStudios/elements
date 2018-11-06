/* tslint:disable */
import { GameOnMatchSummary } from './game-on-match-summary';
import { GameOnPlayerMatchSummary } from './game-on-player-match-summary';

/**
 * Aggregates standard matches and player matches.
 */
export interface GameOnMatchesAggregate {
  matches?: Array<GameOnMatchSummary>;
  playerMatches?: Array<GameOnPlayerMatchSummary>;
}
