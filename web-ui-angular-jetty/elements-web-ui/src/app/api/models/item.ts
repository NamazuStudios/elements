/* tslint:disable */
export interface Item {
  id?: string;
  name: string;
  tags?: Array<string>;
  displayName: string;
  description: string;
  category: string;
  metadata?: {[key: string]: any};
}

export enum ItemCategory {
  FUNGIBLE = "FUNGIBLE",
  DISTINCT = "DISTINCT"
}
