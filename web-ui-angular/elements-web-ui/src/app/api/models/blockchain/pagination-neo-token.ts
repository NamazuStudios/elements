import { NeoToken } from "./neo-token";

/* tslint:disable */
export interface PaginationNeoToken {
  offset?: number;

  total?: number;

  approximation?: boolean;

  objects?: NeoToken[];
}
