import {User} from './user';
import {Item} from './item';

export interface FungibleInventoryItem {
  id: string;
  user: User;
  item: Item;
  quantity: number;
  priority: number;
}
