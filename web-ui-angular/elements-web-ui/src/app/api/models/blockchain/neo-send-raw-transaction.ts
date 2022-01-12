import { Error } from "./error";
import { RawTransaction } from "./raw-transaction";

/* tslint:disable */
export interface NeoSendRawTransaction {
  id?: number;

  jsonrpc?: string;

  result?: RawTransaction;

  error?: Error;

  rawResonse?: string;

  sendRawTransaction?: RawTransaction;
}
