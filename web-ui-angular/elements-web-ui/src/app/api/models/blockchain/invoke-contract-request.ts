import { ContractParameter } from "./contract-parameter";

/* tslint:disable */
export interface PatchNeoSmartContractRequest {

  /**
   * The unique elements ID of the contract to invoke a method on.
   */
   contractId?: string;

  /**
   * The elements wallet Id with funds to invoke the method. This will always use the default account of the wallet.
   */
   walletId: string;

  /**
   * The password of the wallet with funds to mint.
   */
   password: string;

  /**
   * The name of the method to invoke.
   */
   methodName: string;

  /**
   * description:The parameters for the method.
   */
   parameters?: Array<Object>
  
}
