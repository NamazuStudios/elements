/* tslint:disable */
import { Oauth2AuthScheme } from "./auth-scheme-oauth2";
export interface PaginationOauth2AuthScheme {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<Oauth2AuthScheme>;
}
