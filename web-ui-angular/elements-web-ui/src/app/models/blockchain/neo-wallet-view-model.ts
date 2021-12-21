import { User } from "../../api/models";
import { NeoWallet } from "../../api/models/blockchain/neo-wallet";
import { NEP6Wallet } from "../../api/models/blockchain/nep-6-wallet";

export class NeoWalletViewModel implements NeoWallet {
  /**
   * The unique ID of the wallet itself.
   */
  id: string;

  /**
   * The name given to this wallet.
   */
  displayName: string;

  wallet?: NEP6Wallet; // get walletId from here

  user?: User; // get newUserId from here

  userId?: string; // this is the current wallet owner's id

  password?: string;

  newPassword?: string;
}
