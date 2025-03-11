import { NeoSmartContract } from "src/app/api/models/blockchain/neo-smart-contract";

export class NeoSmartContractViewModel implements NeoSmartContract {
  /**
   * The unique ID of the contract itself.
   */
   id: string;

   /**
    * The name given to this contract.
    */
   displayName: string;

   /**
    * The script hash of the contract from the blockchain.
    */
   scriptHash: string;

   /**
    * The blockchain where this contract lives. Valid values are "NEO" : This contract exists on the NEO blockchain network.
    */
   blockchain: string;

   /**
    * description:	Any meta data for this contract.
    */
   metadata?: { [key: string]: any };
}
