import {Item} from './item';

export interface Reward {
  id?: string;
  item?: Item;
  quantity: number;
  metadata: {[key: string]: any};
}
