/* tslint:disable */
import { NeoWallet } from './neo-wallet';
export interface PaginationNeoWallet {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<NeoWallet>;
}
