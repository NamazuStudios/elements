import { User } from "../user";
import { NEP6Wallet } from "./nep-6-wallet";

/* tslint:disable */
export interface NeoWallet {
  /**
   * The unique ID of the wallet itself.
   */
  id: string;

  /**
   * The name given to this wallet.
   */
  displayName: string;

  wallet?: NEP6Wallet;

  user?: User;
}
