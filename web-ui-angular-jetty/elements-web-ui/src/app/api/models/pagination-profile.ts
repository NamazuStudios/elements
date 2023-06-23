/* tslint:disable */
import { Profile } from './profile';
export interface PaginationProfile {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<Profile>;
}
