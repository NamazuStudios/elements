/* tslint:disable */
import { Item } from './item';
export interface PaginationItem {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<Item>;
}
