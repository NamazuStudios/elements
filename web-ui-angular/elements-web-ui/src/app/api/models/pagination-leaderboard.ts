/* tslint:disable */
import { Leaderboard } from './leaderboard';
export interface PaginationLeaderboard {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<Leaderboard>;
}
