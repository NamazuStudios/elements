import {User} from './user';
import {Item} from './item';
import {Profile} from './profile';

export interface DistinctInventoryItem {
  id: string;
  item: Item;
  user: User;
  profile: Profile;
}
