import {User} from './user';
import {Item} from './item';

export interface InventoryItemAdvanced {
  userId: string;
  itemId: string;
  quantity: number;
  priority: number;
}
