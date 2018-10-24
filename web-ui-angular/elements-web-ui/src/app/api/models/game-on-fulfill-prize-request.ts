/* tslint:disable */

/**
 * Corresponds to the GameOn Fulfill Prize List Request:  https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfillprizelistrequest
 */
export interface GameOnFulfillPrizeRequest {

  /**
   * Specifes the device OS Type.
   */
  deviceOSType?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';

  /**
   * Specifies the application build type.
   */
  appBuildType?: 'development' | 'release';

  /**
   * A list of awarded prize IDs.
   */
  awardedPrizeIds?: Array<string>;
}
