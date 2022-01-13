import { StakeHolder } from "./stake-holder";

/* tslint:disable */
export interface Ownership {
  /**
   * description:The list of stakeholders that will be assigned when minting this token.
   */
  stakeHolders: Array<StakeHolder>;

  /**
   * The total number of shares allocated to this token.
   */
  capitalization: number;
}
