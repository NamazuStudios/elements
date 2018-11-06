/* tslint:disable */
import { Friend } from './friend';
export interface PaginationFriend {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<Friend>;
}
