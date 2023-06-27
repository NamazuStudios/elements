/* tslint:disable */
import { User } from './user';
export interface PaginationUser {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<User>;
}
