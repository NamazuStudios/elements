import {Item, ItemCategory} from '../api/models/item';
import {MetadataSpec} from "../api/models/token-spec-tab";

export class ItemViewModel implements Item {
  name: string;
  description: string;
  id: string;
  tags: Array<string>;
  displayName: string;
  metadata: {[key: string]: any};
  category: ItemCategory;
  metadataSpec: MetadataSpec;
  publicVisible: boolean;
}

