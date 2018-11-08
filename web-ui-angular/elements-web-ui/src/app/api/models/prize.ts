/* tslint:disable */

/**
 * The Prize metadata itself.  See: https://developer.amazon.com/docs/gameon/admin-api-ref.html#addprizelistrequest_prize
 */
export interface Prize {

  /**
   * The title of the prize.
   */
  title: string;

  /**
   * A brief description of the prize.
   */
  description: string;

  /**
   * The image URL for the prize.  This may be blank.
   */
  imageUrl?: string;

  /**
   * The prize-info. Additional arbitrary metadata, used for claiming the prize later.  This should reflect, for example, an internal ID for the prize itself used by the application to uniquely identify it later.
   */
  prizeInfo: string;
}
