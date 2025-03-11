/* tslint:disable */
import { OidcAuthScheme } from "./auth-scheme-oidc";
export interface PaginationOidcAuthScheme {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<OidcAuthScheme>;
}
