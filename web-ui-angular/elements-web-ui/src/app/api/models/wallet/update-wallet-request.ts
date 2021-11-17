/* tslint:disable */
/**
 * Represents a request to update a Neo Wallet.
 */
export interface UpdateWalletRequest {
  /**
   * The display name of the wallet.
   */
  displayName: string;
  /**
   * The unique ID of the wallet itself.
   */
  userId?: string;
  /**
   * The new password to encrypt the wallet.
   */
  newPassword?: string;

  id?: string;
}
