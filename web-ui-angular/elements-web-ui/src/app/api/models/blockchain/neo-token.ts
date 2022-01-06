import { Token } from "./token";

/* tslint:disable */
export interface NeoToken {
  /**
   * The unique ID of the token itself.
   */
  id: string;

  token?: Token;

  /**
   * The elements contract id to mint this token with.
   */
  contractId: string;

  listed: boolean;

  minted: boolean;
}
