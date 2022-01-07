/* tslint:disable */
export interface MintTokenRequest {

  /**
   * description:The unique ID's of the tokens to mint.
   */
   tokenIds: Array<string>;

  /**
   * The public address of the account with funds to mint.
   */
   address: string;

}
