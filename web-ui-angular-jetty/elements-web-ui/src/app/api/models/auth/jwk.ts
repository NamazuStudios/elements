/* tslint:disable */
export interface JWK {

  /**
   * Encryption algorithm
   */
  alg: string;

  /**
   * Key id
   */
  kid: string;

  kty: string;

  use: string;

  e: string;

  n: string;
}
