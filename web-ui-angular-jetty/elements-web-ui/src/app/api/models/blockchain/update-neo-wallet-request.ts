/* tslint:disable */
/**
 * Represents a request to update a Neo Wallet.
 */
export interface UpdateNeoWalletRequest {
  /**
   * The new display name of the wallet.
   */
  displayName?: string;
  /**
   * The user Id of the current wallet owner. If left null the current logged in user will be assumed to be the wallet owner.
   */
  userId?: string;
  /**
   * The user Id of the new wallet owner.
   */
   newUserId?: string;
  /**
   *  The current password used to log into the wallet.
   */
   password?: string;

  /**
   *  The new password to be used to encrypt the wallet.
   */
  newPassword?: string;

  walletId:	string
}
