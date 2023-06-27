import { Vault } from "./vaults";

export interface ContractAddresses {
  [key: string]: {
    address: string
  };
}

export interface Contract {
  id: string;
  name: string;
  displayName: string;
  addresses: ContractAddresses;
  vault: Vault;
  metadata: any;
}


export interface CreateContractRequest {
  name?: string;
  displayName?: string;
  addresses?: ContractAddresses;
  vaultId?: string;
  metadata?: any;
}

export interface UpdateContractRequest {
  name?: string;
  displayName?: string;
  addresses?: ContractAddresses;
  vaultId?: string;
  metadata?: any;
}
