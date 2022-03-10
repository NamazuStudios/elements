import {FungibleInventoryItem} from './fungible-inventory-item';

export interface PaginationFungibleInventoryItem {
  offset?: number;
  total?: number;
  approximation?: number;
  objects?: Array<FungibleInventoryItem>;
}
