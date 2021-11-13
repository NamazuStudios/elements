import {User} from './user';
import {Item} from './item';

export interface InventoryItem {
  id: string;
  user: User;
  item: Item;
  quantity: number;
  priority: number;
}
