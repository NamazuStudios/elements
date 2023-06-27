/* tslint:disable */
import { Application } from './application';
export interface PaginationApplication {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<Application>;
}
