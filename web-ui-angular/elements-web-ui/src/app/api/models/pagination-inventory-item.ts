import {InventoryItem} from './inventory-item';

export interface PaginationInventoryItem {
  offset?: number;
  total?: number;
  approximation?: number;
  objects?: Array<InventoryItem>;
}
