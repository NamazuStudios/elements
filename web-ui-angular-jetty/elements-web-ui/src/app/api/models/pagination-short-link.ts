/* tslint:disable */
import { ShortLink } from './short-link';
export interface PaginationShortLink {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<ShortLink>;
}
