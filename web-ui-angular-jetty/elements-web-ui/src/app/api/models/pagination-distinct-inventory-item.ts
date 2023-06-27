import {DistinctInventoryItem} from './distinct-inventory-item';

export interface PaginationDistinctInventoryItem {
  offset?: number;
  total?: number;
  approximation?: number;
  objects?: Array<DistinctInventoryItem>;
}
