/* tslint:disable */

export interface AppleSignInConfiguration {

  /**
   * The Apple Sign-In Private Key
   */
  keyId: string,

  /**
   * The Apple Developer Team ID
   */
  teamId: string,

  /**
   * The ClientID to use when verifying the JWT token.
   */
  clientId: string,

  /**
   * The PEM encoded private key issued by the Apple to verify the JWT sign-in token.
   */
  appleSignInPrivateKey: string

}
