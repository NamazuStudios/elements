/* tslint:disable */
import { Match } from './match';
export interface MatchesPagination {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<Match>;
}
