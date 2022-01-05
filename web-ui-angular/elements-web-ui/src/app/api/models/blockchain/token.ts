import { Ownership } from "./ownership";

/* tslint:disable */
export interface Token {
  /**
   * The account address of the owner to be assigned when minting this token.
   */
  owner: string;

  /**
   * The name given to this token
   */
  name: string;

  /**
   * The description of this token.
   */
  description?: string;

  /**
   * Any tags to assist in filtering/searching for this token.
   */
  tags: Array<string>;

  /**
   * The maximum number of copies of this token that can be owned (by any number of accounts) at any one time.
   */
  totalSupply: number;

  /**
   * The status of this token. Valid values are "public" : Can be viewed by everyone, "private" : Only the token or contract owner can view the token properties "preview" : If not the token or contract owner, the asset urls cannot be viewed.
   */
  accessOption: "public" | "private" | "preview";

  /**
   * The URL pointed at any preview of the contents of this token.
   */
  previewUrls: Array<string>;

  /**
   * The asset URLs of this token.
   */
  assetUrls: Array<string>;

  ownership?: Ownership;

  /**
   * The transfer options of this token. Valid values are "none" : Cannot be transferred, "resale_only" : Can be resold, but not traded, "trades_only" : Can be traded, but not resold, and "resale_and_trades" : Can be either resold or traded.
   */
  transferOptions: "none" | "resale_only" | "trades_only" | "resale_and_trades";

  /**
   * Indicates whether or not the license is revocable by the owner
   */
  revocable?: boolean;

  /**
   * The expiration date of the license. Recorded in seconds since Unix epoch
   */
  expiry?: number;

  /**
   * If true, the licensee may pay a fee to extend the expiration date by the same difference between the original expiry and the time of minting.
   */
  renewable?: boolean;

  /**
   * description:	Any meta data for this token.
   */
  metadata?: { [key: string]: any };
}
