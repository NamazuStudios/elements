/* tslint:disable */

/**
 * Represents a request to update a Neo Token.
 */

export interface UpdateTokenRequest {
  /**
   * The id of the deployed token to update.
   */
  tokenId: string;

  /**
   * The id of the smart contract template to deploy.
   */
  templateId: string;

  /**
   * The name of this token.
   */
  name: string;

  /**
   * The description of this token.
   */
  description?: string;

  /**
   * The type of this token. Valid options are "purchase" : ownership is transferred to the purchaser, "license" : the minter of the token retains ownership, but grants access to the purchaser, and "rent" : same as license, but access is revoked after a certain period of time (see rentDuration).
   */
  type: "purchase" | "license" | "rent";

  /**
   * Any tags to assist in filtering/searching for this token.
   */
  tags: string[];

  /**
   * The royalty percentage to be processed on resale, if any.
   */
  royaltyPercentage?: number;

  /**
   * The duration of the rental before it is automatically returned (in seconds). Only valid for rent type tokens
   */
  rentDuration?: number;

  /**
   * The quantity of copies of this token that can be distributed.
   */
  quantity?: number;

  /**
   * The transfer options of this token. Valid values are "none" : Cannot be transferred, "resale_only" : Can be resold, but not traded, "trades_only" : Can be traded, but not resold, and "resale_and_trades" : Can be either resold or traded.
   */
  transferOptions: "none" | "resale_only" | "trades_only" | "resale_and_trades";

  /**
   * Indicates whether or not this can be viewed publicly. If false, only the previewUrl can be viewed publicly.
   */
  publiclyAccessible: boolean;

  /**
   * The URL pointed at any preview of the contents of this token.
   */
  previewUrl?: string;

  /**
   * The asset URLs of this token.
   */
  assetUrls: string[];
}
