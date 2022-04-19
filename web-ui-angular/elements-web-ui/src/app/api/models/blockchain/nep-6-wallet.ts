import { NEP6Account } from "./nep-6-account";
import { ScryptParams } from "./scrypt-params";

/* tslint:disable */
export interface NEP6Wallet {
  readonly name?: string;

  readonly version?: string;

  scrypt?: ScryptParams;

  readonly accounts?: NEP6Account[];

  extra?: any;
}
