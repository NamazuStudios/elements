/* tslint:disable */

import { Token } from "./token";

/**
 * Represents a request to update a Neo Token.
 */

export interface UpdateNeoTokenRequest {
  token: Token;

  /**
   * Is this token listed for sale?
   */
  listed: boolean;

  /**
   * description:	Any meta data for this token.
   */
  metadata?: { [key: string]: any };
}
