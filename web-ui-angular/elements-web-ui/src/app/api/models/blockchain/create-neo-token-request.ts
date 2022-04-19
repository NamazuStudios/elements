/* tslint:disable */

import { Token } from "./token";

/**
 * Represents a request to create a NeoToken definition.
 */
export interface CreateNeoTokenRequest {
  token: Token;

  /**
   * Is this token listed.
   */
  listed: boolean;

  /**
   * The elements contract id to mint this token with.
   */
  contractId: string;
}
