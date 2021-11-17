import { Token } from "./token";

/* tslint:disable */
export interface PaginationToken {
  offset?: number;

  total?: number;

  approximation?: boolean;

  objects: Token[];
}
