/* tslint:disable */
export interface MintTokenRequest {
  /**
   * description:The unique ID's of the tokens to mint.
   */
  tokenIds: Array<string>;

  /**
   * The elements wallet Id with funds to invoke the method. This will always use the default account of the wallet.
   */
  walletId: string;

  /**
   * The password of the wallet with funds to mint.
   */
  password: string;
}
