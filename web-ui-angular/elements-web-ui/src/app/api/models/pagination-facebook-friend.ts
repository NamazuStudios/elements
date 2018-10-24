/* tslint:disable */
import { FacebookFriend } from './facebook-friend';
export interface PaginationFacebookFriend {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<FacebookFriend>;
}
