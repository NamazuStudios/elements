/* tslint:disable */
import { NeoSmartContract } from "./neo-smart-contract";

export interface PaginationNeoSmartContract {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<NeoSmartContract>;
}
