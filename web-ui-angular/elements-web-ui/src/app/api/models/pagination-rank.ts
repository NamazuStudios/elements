/* tslint:disable */
import { Rank } from './rank';
export interface PaginationRank {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<Rank>;
}
