import { ContractParameter } from "./contract-parameter";

/* tslint:disable */
export interface PatchNeoSmartContractRequest {

  /**
   * The unique elements ID of the contract to invoke a method on.
   */
   contractId: string;

  /**
   * The address of the account with funds to invoke.
   */
   address: string;

  /**
   * The name of the method to invoke.
   */
   methodName: string;

  /**
   * description:The parameters for the method.
   */
   parameters?: Array<ContractParameter>
  
}
