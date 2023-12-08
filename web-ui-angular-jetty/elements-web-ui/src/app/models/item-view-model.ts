import {Item, ItemCategory} from '../api/models/item';

export class ItemViewModel implements Item {
  name: string;
  description: string;
  id: string;
  tags: Array<string>;
  displayName: string;
  metadata: {[key: string]: any};
  category: ItemCategory;
  publicVisible: boolean;
}

