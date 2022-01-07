/* tslint:disable */
import { AuthScheme } from "./authScheme";
export interface PaginationAuthScheme {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<AuthScheme>;
}
