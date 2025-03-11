/* tslint:disable */
import {MetadataSpec} from "./token-spec-tab";

export interface Item {
  id?: string;
  name: string;
  tags?: Array<string>;
  displayName: string;
  description: string;
  category: string;
  metadataSpec: MetadataSpec;
  publicVisible: boolean;
  metadata?: {[key: string]: any};
}

export enum ItemCategory {
  FUNGIBLE = "FUNGIBLE",
  DISTINCT = "DISTINCT"
}
