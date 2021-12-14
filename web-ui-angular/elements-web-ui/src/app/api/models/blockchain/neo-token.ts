import { Token } from "./token";

/* tslint:disable */
export interface NeoToken {
  /**
   * The unique ID of the token itself.
   */
  id: string;

  token?: Token;

  metadata?: { [key: string]: any };

  contract?: string;

  listed?: boolean;

  minted?: boolean;
}
