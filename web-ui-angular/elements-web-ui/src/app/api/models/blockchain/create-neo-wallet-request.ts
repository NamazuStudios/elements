/* tslint:disable */
/**
 * Represents a request to create a neo wallet.
 */
export interface CreateNeoWalletRequest {
  /**
   * A user-defined name for the wallet..
   */
  displayName: string;
  /**
   * The elements-defined user ID to own the wallet.
   */
  userId?: string;
  /**
   * Password to encrypt the wallet.
   */
  password?: string;
}
