/* tslint:disable */
import { CustomAuthScheme } from "./auth-scheme-custom";
export interface PaginationCustomAuthScheme {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<CustomAuthScheme>;
}
