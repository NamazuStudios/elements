/* tslint:disable */

import { Token } from "./token";

/**
 * Represents a request to create a NeoToken definition.
 */
export interface CreateNeoTokenRequest {
  token: Token;

  /**
   * description:	Any meta data for this token.
   */
  metadata?: { [key: string]: any };
}
