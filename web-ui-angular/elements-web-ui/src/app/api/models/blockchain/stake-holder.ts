/* tslint:disable */
export interface StakeHolder {
  /**
   * The account address of the stakeholder to be assigned when minting this token.
   */
  owner: string;

  /**
   * If true, allows for voting on any proposed change.
   */
  voting: boolean;

  /**
   * The number of shares assigned to the Stakeholder.
   */
  shares: number;
}
