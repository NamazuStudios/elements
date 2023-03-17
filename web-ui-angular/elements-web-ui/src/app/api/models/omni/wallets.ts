import { User } from "../user";
import { Vault } from "./vaults";

export interface WalletAccount {
  generate: boolean;
  address: string;
  privateKey: string;
}

export interface Wallet {
  id: string;
  displayName: string;
  api: string;
  accounts: WalletAccount[];
  networks: string[];
  preferredAccount: number;
  user: User;
  vault: Vault;
}

export interface CreateWalletRequest {
  displayName: string;
  api: string;
  networks: string[];
  preferredAccount: number;
  accounts: WalletAccount[];
}

export interface UpdateWalletRequest {
  displayName?: string;
  preferredAccount?: number;
  networks: string[];
}
