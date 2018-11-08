/* tslint:disable */
export interface Item {
  id?: string;
  name: string;
  tags?: Array<string>;
  displayName: string;
  description: string;
  metadata?: {[key: string]: string};
}
