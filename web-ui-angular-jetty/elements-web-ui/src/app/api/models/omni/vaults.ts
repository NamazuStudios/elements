import type { User } from "../user";

export interface Vault {
  id: string;
  user: User;
  displayName: string;
  key: {
    algorithm: string;
    publicKey: string;
    privateKey: string;
    encrypted: boolean;
    encryption: any;
  }
}

export interface CreateVaultParams {
  userId: string;
  displayName: string;
  passphrase: string;
  algorithm?: string;
}

export interface UpdateVaultRequest {
  displayName?: string;
  userId?: string;
  passphrase?: string;
  newPassphrase?: string;
}
